package io.fabric8.memcached.operator.memcached_types;

public class MemcachedStatus {

    private  int availableReplicas;

    public int getAvailableReplicas() {
        return availableReplicas;
    }

    public void setAvailableReplicas(int availableReplicas) {
        this.availableReplicas = availableReplicas;
    }
}
