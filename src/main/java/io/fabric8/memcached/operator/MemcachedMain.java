package io.fabric8.memcached.operator;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class MemcachedMain {

    public static void main(String args[]){
        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
        String nameSpace = kubernetesClient.getNamespace();

        System.out.println("Namspace"+nameSpace);

        System.out.println("Api Version"+kubernetesClient.getApiVersion());
        System.out.println(" Version"+kubernetesClient.getVersion());
        System.out.println(" config"+kubernetesClient.getConfiguration());
        System.out.println(" masterurl"+kubernetesClient.getMasterUrl());
        System.out.println(" class"+kubernetesClient.getClass());

        if(nameSpace == null){
            System.out.println("NameSpace is EMpty, Assigned an Default Namespace");
            nameSpace = "default";
        }

        System.out.println("We are Using Namespace = " + nameSpace);



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
