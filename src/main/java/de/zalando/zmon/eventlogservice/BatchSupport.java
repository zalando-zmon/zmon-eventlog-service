package de.zalando.zmon.eventlogservice;

import java.util.List;

import com.google.common.collect.Iterables;

/**
 * To introduce support for batching.
 * 
 * @author jbellmann
 *
 */
public interface BatchSupport {

    default boolean isBatchSupported() {
        return false;
    }

    default <T> void storeInBatch(Iterable<T> toStore) {
        throw new RuntimeException("Not implemented yet.");
    }

    default <T> Iterable<List<T>> partition(Iterable<T> toPartition) {
        return Iterables.partition(toPartition, getPartitionSize());
    }

    default int getPartitionSize() {
        return 20;
    }

}
