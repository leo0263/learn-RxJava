import io.reactivex.Observable

fun main(args: Array<String>) {

    exampleOf("just") {
        val observable: Observable<Int> = Observable.just(1)
    }

}
