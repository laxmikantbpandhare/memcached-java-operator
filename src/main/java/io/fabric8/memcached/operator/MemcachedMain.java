package io.fabric8.memcached.operator;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.memcached.operator.controller.MemcachedController;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import io.fabric8.memcached.operator.memcached_types.MemcachedList;

public class MemcachedMain {

    public static void main(String args[]) throws InterruptedException {
        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
//        String nameSpace = kubernetesClient.getNamespace();
//
//        if(nameSpace == null){
//            System.out.println("NameSpace is Empty, Assigned an Default Namespace");
//            nameSpace = "default";
//        }

        CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1alpha1")
                .withScope("Namespaces")
                .withGroup("demo.k8s.io")
                .withPlural("podsets")
                .build();

        SharedInformerFactory sharedInformerFactory = kubernetesClient.informers();

        SharedIndexInformer<Pod> podSharedIndexInformer =  sharedInformerFactory.sharedIndexInformerFor(Pod.class, PodList.class,10 * 60 * 1000);

        SharedIndexInformer<Memcached> memcachedSharedIndexInformer = sharedInformerFactory
                .sharedIndexInformerForCustomResource(customResourceDefinitionContext,Memcached.class, MemcachedList.class,5 * 60 * 1000);

        MemcachedController memcachedController =  new MemcachedController(kubernetesClient,podSharedIndexInformer,memcachedSharedIndexInformer,sharedInformerFactory);
        memcachedController.create();
        memcachedController.run();
    }
}
