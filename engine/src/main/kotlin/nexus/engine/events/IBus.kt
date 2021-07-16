package nexus.engine.events

interface IBus {
    fun subscribe(listener: Any)

    fun <T : IEvent> publish(event: T)

    fun <T : IEvent> publishAsync(event: T)

    fun shutdown()
}