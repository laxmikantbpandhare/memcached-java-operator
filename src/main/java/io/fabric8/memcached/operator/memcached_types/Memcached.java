package io.fabric8.memcached.operator.memcached_types;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.CustomResource;

public class Memcached extends CustomResource {

    private MemcachedSpec spec;
    private MemcachedStatus Status;
}
