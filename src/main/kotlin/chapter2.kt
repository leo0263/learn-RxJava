import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.pow
import kotlin.math.roundToInt

fun main(args: Array<String>) {

    exampleOf("just") {
        val observable1: Observable<Int> = Observable.just(1)
        val observable2 = Observable.just(1, 2, 3)
        // both observable1 & observable2 type is Observable<Int>
        // the observable2 will emit three Int item

        // this is how to make it emit a list of Int (emitting one item)
        val observable3 = Observable.just(listOf(1, 2, 3))
    }

    exampleOf("fromIterable") {
        // from iterable will break the list down:
        val observable: Observable<Int> = Observable.fromIterable(listOf(1, 2, 3))
        // this will make the observable type to become Observable<Int> rather than Observable<List<Int>>
    }

    exampleOf("subscribe") {
        val observable = Observable.just(1, 2, 3)

        observable.subscribe {
            println(it)
        }

        // note: Observable won't send events until it has a subscriber!
    }

    exampleOf("empty") {
        val observable = Observable.empty<Unit>()

        // note: subscribeBy is RxKotlin library extension!
        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("completed") }
        )

        // empty observable is handy when we want to return an observable that:
        // immediately terminates or intentionally return zero values
    }

    exampleOf("never") {
        val observable = Observable.never<Any>()

        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("completed") }
        )

        // never will not print anything, not even the completed text!
        // by using the never operator, we create an observable that:
        // doesn't emit anything, and never terminates! -> can be used to represent infinite duration
    }

    exampleOf("range") {
        val observable = Observable.range(1, 10)

        observable.subscribe {
            val n = it.toDouble()
            val fibonacci = ((1.61803.pow(n) - 0.61803.pow(n)) / 2.23606).roundToInt()
            println(fibonacci)
        }
    }

    exampleOf("dispose") {
        val mostPopular = Observable.just("A", "B", "C")

        val subscription = mostPopular.subscribe {
            println(it)
        }
        // note: a subscribe() return value is <Disposable>

        subscription.dispose()
        // note: if we don't dispose manually, it may trigger memory leak!
        // this can happen when an observable doesn't reach it's success or error state
    }

    exampleOf("CompositeDisposable") {
        val subscriptions = CompositeDisposable()

        val disposableString = Observable.just("A", "B", "C")
            .subscribeBy(
                onNext = { println(it) },
                onComplete = { println("complete disposableString") }
            )

        val disposableInt = Observable.just(1, 2, 3)
            .subscribeBy(
                onNext = { println(it) },
                onComplete = { println("complete disposableInt") }
            )

        println("done creating disposable string & int")

        // note: CompositeDisposable can be used to manage several disposables
        // it require O(1) for addition and removal
        subscriptions.add(disposableInt)
        subscriptions.add(disposableString)
        subscriptions.dispose()
    }

    exampleOf("create") {
        // create defines all the events that will be emitted to subscriber

        val observable = Observable.create<String> { emitter ->
            emitter.onNext("1")
            //emitter.onError(RuntimeException("Error"))
            //emitter.onComplete()
            emitter.onNext("?")
        }.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("completed") },
            onError = { println(it) }
        )

        // note: by disabling onError and/or onComplete, we've created a leaky memory state
        // the observable sequence will never be cancelled, unless we dispose it!
        observable.dispose()
    }

    exampleOf("defer") {
        val disposables = CompositeDisposable()
        var flip = false

        val factory: Observable<Int> = Observable.defer {
            flip = !flip
            if (flip) {
                Observable.just(1, 2, 3)
            } else {
                Observable.just(4, 5, 6)
            }
        }

        for (i in 0..3) {
            disposables.add(
                factory.subscribeBy(
                    onNext = { print("$it ") },
                    onComplete = { println() }
                )
            )
        }
    }

    exampleOf("single") {
        val subscriptions = CompositeDisposable()

        fun loadText(filename: String): Single<String> {
            return Single.create create@{ emitter ->
                val file = File(filename)
                if (!file.exists()) {
                    emitter.onError(FileNotFoundException("can't find file $filename"))
                    return@create
                }

                val contents = file.readText()
                emitter.onSuccess(contents)
            }
        }

        val observer = loadText("dangdut.txt").subscribeBy(
            onSuccess = { println(it) },
            onError = { println("Error $it") }
        )

        subscriptions.add(observer)
        subscriptions.dispose()

        // there are three special types of observable
        // 1. Single -> emit either success(value) or error
        // 2. Completable -> emit completed or error
        // 3. Maybe -> emit success(value), completed, or error (combination of Single & Completable)
    }
}
