package io.fabric8.memcached.operator.controller;

import io.fabric8.controller.controller_runtime.pkg.Reconciler;
import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.controller.controller_runtime.pkg.Result;

public class MemcachedReconciler implements Reconciler {

    @Override
    public Result reconcile(Request request) {
        System.out.println("calling reconcile from default controller");
        return null;
    }
}
