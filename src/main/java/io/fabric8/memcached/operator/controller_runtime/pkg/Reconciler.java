package io.fabric8.memcached.operator.controller_runtime.pkg;

import io.fabric8.memcached.operator.memcached_types.Memcached;

public interface Reconciler {

    void reconcile(Memcached memcached);
}
