package io.fabric8.memcached.operator.controller;

import io.fabric8.controller.controller_runtime.Controller;
import io.fabric8.controller.controller_runtime.Controllers;
import io.fabric8.controller.controller_runtime.DefaultController;
import io.fabric8.controller.controller_runtime.Manager;
import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;

public class MemcachedController {
    private static final Logger logger = LoggerFactory.getLogger(MemcachedController.class);

    public KubernetesClient kubernetesClient;
    public SharedIndexInformer<Pod> podSharedIndexInformer;
    public SharedIndexInformer<Memcached> memcachedSharedIndexInformer;
    private BlockingQueue<Request> workQueue;
    private Lister<Memcached> memcachedLister;
    private Lister<Pod> podLister;
    private Controller[] controllers;
    SharedInformerFactory sharedInformerFactory;
    private int workerCount = 2;
    private String controllerName = "memcached-controller";
    DefaultController defaultController;

    public MemcachedController(KubernetesClient kubernetesClient, SharedIndexInformer<Pod> podSharedIndexInformer, SharedIndexInformer<Memcached> memcachedSharedIndexInformer,SharedInformerFactory sharedInformerFactory ){
        this.kubernetesClient = kubernetesClient;
        this.podSharedIndexInformer = podSharedIndexInformer;
        this.memcachedSharedIndexInformer = memcachedSharedIndexInformer;
        this.workQueue = new ArrayBlockingQueue<>(1024);
        this.memcachedLister = new Lister<>(memcachedSharedIndexInformer.getIndexer(),"default");
        this.podLister = new Lister<>(podSharedIndexInformer.getIndexer(),"default");
        defaultController = new DefaultController();
        this.sharedInformerFactory = sharedInformerFactory;
    }

    public MemcachedController() {

    }

    public void create(){

        memcachedSharedIndexInformer.addEventHandler(new ResourceEventHandler<Memcached>() {
            @Override
            public void onAdd(Memcached memcached) {
                enQueueMemcached(memcached);
            }

            @Override
            public void onUpdate(Memcached memcached, Memcached newMemcached) {
             //   enQueueMemcached(newMemcached);
            }

            @Override
            public void onDelete(Memcached memcached, boolean b) {
//                System.out.println("memcachedSharedIndexInformer onDelete method Function pods");
            }
        });

        podSharedIndexInformer.addEventHandler(new ResourceEventHandler<Pod>() {
            @Override
            public void onAdd(Pod pod) {
                handlePodObject(pod);
            }

            @Override
            public void onUpdate(Pod oldPod, Pod newPod) {
                handlePodObject(newPod);
            }

            @Override
            public void onDelete(Pod pod, boolean b) {
                handlePodObject(pod);
            }
        });
    }

    public void initializeDefaultController(){
        System.out.println("I am in Thread run() method");
        MemcachedReconciler reconciler = new MemcachedReconciler(kubernetesClient,podLister,memcachedLister);
        defaultController.setWorkQueue(workQueue);
        defaultController.setName(this.controllerName);
        defaultController.setWorkerCount(this.workerCount);
        defaultController.setWorkerThreadPool(
                Executors.newScheduledThreadPool(
                        this.workerCount, Controllers.namedControllerThreadFactory(this.controllerName)));
        defaultController.setReconciler(reconciler);
        this.controllers = new Controller[]{defaultController};
    }

    public void run() throws InterruptedException {
        this.initializeDefaultController();
        Manager manager = new Manager(sharedInformerFactory,controllers);
        manager.run();
    }

    private void enQueueMemcached(Memcached memcached){
        String key = Cache.metaNamespaceKeyFunc(memcached);;
        if(key!=null || !(key.isEmpty())){
            workQueue =  defaultController.getWorkQueue();
            workQueue.add(new Request(memcached.getMetadata().getNamespace(), memcached.getMetadata().getName()));
            defaultController.setWorkQueue(workQueue);
        }
    }

    private void handlePodObject(Pod pod){
        OwnerReference ownerReference = getController(pod);
        if(!ownerReference.getKind().equalsIgnoreCase("Podset")){
            return;
        }
        Memcached memcached =  memcachedLister.get(ownerReference.getName());
        if(memcached!=null)
            enQueueMemcached(memcached);
    }

    private OwnerReference getController(Pod pod){
        List<OwnerReference> ownerReferenceList = pod.getMetadata().getOwnerReferences();
        for(OwnerReference ownerReference : ownerReferenceList){
            if(ownerReference.getController().equals(Boolean.TRUE)){
                return ownerReference;
            }
        }
        return null;
    }
}
