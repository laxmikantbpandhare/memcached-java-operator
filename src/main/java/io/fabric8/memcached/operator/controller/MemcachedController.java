package io.fabric8.memcached.operator.controller;


import io.fabric8.controller.controller_runtime.DefaultController;
import io.fabric8.controller.controller_runtime.pkg.Reconciler;
import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.controller.controller_runtime.pkg.Result;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;



import io.fabric8.memcached.operator.memcached_types.Memcached;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MemcachedController {//implements Reconciler {

    public KubernetesClient kubernetesClient;
    public SharedIndexInformer<Pod> podSharedIndexInformer;
    public SharedIndexInformer<Memcached> memcachedSharedIndexInformer;
    private BlockingQueue<String> workQueue;
    private Lister<Memcached> memcachedLister;
    private Lister<Pod> podLister;
   // DefaultController defaultController;


    public MemcachedController(KubernetesClient kubernetesClient, SharedIndexInformer<Pod> podSharedIndexInformer, SharedIndexInformer<Memcached> memcachedSharedIndexInformer){
        this.kubernetesClient = kubernetesClient;
        this.podSharedIndexInformer = podSharedIndexInformer;
        this.memcachedSharedIndexInformer = memcachedSharedIndexInformer;
        this.workQueue = new ArrayBlockingQueue<>(1024);
        this.memcachedLister = new Lister<>(memcachedSharedIndexInformer.getIndexer(),"default");
        this.podLister = new Lister<>(podSharedIndexInformer.getIndexer(),"default");
      //  defaultController = new DefaultController();
    }

    public void create(){

        memcachedSharedIndexInformer.addEventHandler(new ResourceEventHandler<Memcached>() {
            @Override
            public void onAdd(Memcached memcached) {
                enQueueMemcached(memcached);
                System.out.println("memcachedSharedIndexInformer onAdd method Function pods");
            }

            @Override
            public void onUpdate(Memcached memcached, Memcached newMemcached) {
             //   enQueueMemcached(newMemcached);
                System.out.println("memcachedSharedIndexInformer onUpdate method Function pods");
            }

            @Override
            public void onDelete(Memcached memcached, boolean b) {
                System.out.println("memcachedSharedIndexInformer onDelete method Function pods");
            }
        });

        podSharedIndexInformer.addEventHandler(new ResourceEventHandler<Pod>() {
            @Override
            public void onAdd(Pod pod) {
                handlePodObject(pod);
                System.out.println("podSharedIndexInformer onAdd method Function pods");
            }

            @Override
            public void onUpdate(Pod oldPod, Pod newPod) {
                handlePodObject(newPod);
                System.out.println("podSharedIndexInformer onUpdate method Function pods");
            }

            @Override
            public void onDelete(Pod pod, boolean b) {
                System.out.println("podSharedIndexInformer onDelete method Function pods");
            }
        });
    }

    public void run() throws InterruptedException {

        System.out.println("I am in Thread run() method");
        MemcachedReconciler reconciler = new MemcachedReconciler();
        DefaultController defaultController = new DefaultController(reconciler);
        defaultController.runMethod();

        while (!memcachedSharedIndexInformer.hasSynced() || !podSharedIndexInformer.hasSynced());

        while(true){
            System.out.println("while loop method");
            String key = workQueue.take();

            if(key == null || key.isEmpty() || (!key.contains("/"))){
                    //throw error
            }

            String name = key.split("/")[1];
            Memcached memcached =  memcachedLister.get(name);
            if(memcached ==  null) {
                return;
            }
            this.reconcile1(memcached);
        }
    }

    public void reconcile1(Memcached memcached){

        List<String> pods = podCountByLabel("app",memcached.getMetadata().getName());
        System.out.println("Reconcile Function"+memcached.getMetadata().getName());
        for(int i=0;i<pods.size();i++){
            System.out.println("Reconcile Function pods"+ pods.get(i));
        }

//        if(pods == null || pods.size()==0){
//            System.out.println("Size"+ memcached.getSpec().getSize());
//            createPod(memcached.getSpec().getSize(),memcached);
//        }

        int existingPods = pods.size();
        int desiredPods = memcached.getSpec().getSize();
        System.out.println("Reconcile Function desired"+desiredPods);
        System.out.println("Reconcile Function existing"+existingPods);
        if(existingPods < desiredPods){
            System.out.println("in if"+desiredPods);
            createPod(desiredPods-existingPods,memcached);
        }
        else if(desiredPods < existingPods){
            System.out.println("in else"+desiredPods);
            int diff = existingPods - desiredPods;
            System.out.println("Diff"+diff);
            for(int i=0;i<diff;i++) {
                System.out.println("For loop");
                String podName = pods.remove(0);
                kubernetesClient.pods().inNamespace(memcached.getMetadata().getNamespace()).withName(podName).delete();
            }
        }
    }

    private void createPod(int noOfPods, Memcached memcached){
//        System.out.println("Create Pod Gets called");
        for(int i = 0;i<noOfPods;i++){
            Pod pod = createNewPod(memcached);
            kubernetesClient.pods().inNamespace(memcached.getMetadata().getNamespace()).create(pod);
        }
    }



    private Pod createNewPod(Memcached memcached){
//        System.out.println("createNewPod Function"+memcached.getMetadata().getName());
        return new PodBuilder()
                .withNewMetadata()
                .withGenerateName(memcached.getMetadata().getName() + "-pod")
                .withLabels(Collections.singletonMap("app",memcached.getMetadata().getName()))
                .addNewOwnerReference().withController(true).withKind("PodSet").withApiVersion("demo.k8s.io/v1alpha1").withName(memcached.getMetadata().getName()).withNewUid(memcached.getMetadata().getUid()).endOwnerReference()
                .endMetadata()
                .withNewSpec()
                .addNewContainer().withName("busybox").withImage("busybox").withCommand("sleep","3600").endContainer()
                .endSpec()
                .build();
    }

    private List<String> podCountByLabel(String label, String memcachedName){
//        System.out.println("podCountByLabel Function"+memcachedName);
        List<String> podNames = new ArrayList<>();
        List<Pod> pods = podLister.list();

        for(Pod pod : pods) {
            if (pod.getMetadata().getLabels().entrySet().contains(new AbstractMap.SimpleEntry<>(label, memcachedName))) {
                podNames.add(pod.getMetadata().getName());
            }
        }

        return podNames;
    }

    private void enQueueMemcached(Memcached memcached){
        String key = Cache.metaNamespaceKeyFunc(memcached);
//        System.out.println("enQueueMemcached added Pod Gets called"+key);
        if(key!=null || !(key.isEmpty())){
            System.out.println("workqued added Pod Gets called");
            workQueue.add(key);
        }
    }

    private void handlePodObject(Pod pod){
//        System.out.println("handlePodObject added Pod Gets called");
        OwnerReference ownerReference = getController(pod);
        if(!ownerReference.getKind().equalsIgnoreCase("MemCached")){
            return;
        }
        Memcached memcached =  memcachedLister.get(ownerReference.getName());
        System.out.println("handlePodObject memcached added Pod Gets called");
        if(memcached!=null)
            enQueueMemcached(memcached);
    }

    private OwnerReference getController(Pod pod){
//        System.out.println("getController Gets called");
        List<OwnerReference> ownerReferenceList = pod.getMetadata().getOwnerReferences();
        for(OwnerReference ownerReference : ownerReferenceList){
            if(ownerReference.getController().equals(Boolean.TRUE)){
                return ownerReference;
            }
        }
        return null;
    }


//    @Override
//    public Result reconcile(Request request) {
//        System.out.println("calling reconcile from default controller");
//        return null;
//    }
}
