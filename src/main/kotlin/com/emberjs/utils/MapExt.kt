@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> {
    return filter { it.value != null } as Map<K, V>
}
