package com.emberjs.index

import com.emberjs.resolver.EmberName
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput

class EmberNameKeyDescriptor : KeyDescriptor<EmberName> {

    override fun getHashCode(value: EmberName) = value.hashCode()
    override fun isEqual(val1: EmberName?, val2: EmberName?) = (val1 == val2)

    override fun save(storage: DataOutput, value: EmberName) {
        IOUtil.writeUTF(storage, value.storageKey)
    }

    override fun read(storage: DataInput): EmberName? {
        return EmberName.from(IOUtil.readUTF(storage))
    }
}
