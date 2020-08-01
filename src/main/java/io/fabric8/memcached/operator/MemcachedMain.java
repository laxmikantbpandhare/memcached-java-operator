package io.fabric8.memcached.operator;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.memcached.operator.controller.MemcachedController;
import io.fabric8.memcached.operator.controller_runtime.pkg.Request;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import io.fabric8.memcached.operator.memcached_types.MemcachedList;

public class MemcachedMain {

    public static void main(String args[]) throws InterruptedException {
        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
        String nameSpace = kubernetesClient.getNamespace();

//        System.out.println("Namspace"+nameSpace);
//
//        System.out.println("Api Version"+kubernetesClient.getApiVersion());
//        System.out.println(" Version"+kubernetesClient.getVersion());
//        System.out.println(" config"+kubernetesClient.getConfiguration());
//        System.out.println(" masterurl"+kubernetesClient.getMasterUrl());
//        System.out.println(" class"+kubernetesClient.getClass());

        if(nameSpace == null){
            System.out.println("NameSpace is Empty, Assigned an Default Namespace");
            nameSpace = "default";
        }

        Request request;
        CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1alpha1")
                .withScope("Namespaces")
                .withGroup("demo.k8s.io")
                .withPlural("podsets")
                .build();

        System.out.println("We are Using Namespace = " + nameSpace);

        SharedInformerFactory sharedInformerFactory = kubernetesClient.informers();

        SharedIndexInformer<Pod> podSharedIndexInformer =  sharedInformerFactory.sharedIndexInformerFor(Pod.class, PodList.class,60 * 1000);

        SharedIndexInformer<Memcached> memcachedSharedIndexInformer = sharedInformerFactory
                .sharedIndexInformerForCustomResource(customResourceDefinitionContext,Memcached.class, MemcachedList.class,60*1000);

        MemcachedController memcachedController =  new MemcachedController(kubernetesClient,podSharedIndexInformer,memcachedSharedIndexInformer);
        memcachedController.create();
        sharedInformerFactory.startAllRegisteredInformers();
        memcachedController.run();

//        ApiClient client = Config.defaultClient();
//        Configuration.setDefaultApiClient(client);
//
//        CoreV1Api api = new CoreV1Api();
//        V1PodList list =
//                api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
//        for (V1Pod item : list.getItems()) {
//            System.out.println(item.getMetadata().getName());
//        }

//        try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()){
//
//        }
    }
}
