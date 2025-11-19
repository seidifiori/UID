package org.example.ProgettoUIDFinal;

import javafx.application.Platform;
import javafx.scene.image.Image;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * InventoryService: mantiene le immagini possedute e notifica i listener (InventoryRefreshable).
 * Usa WeakReference per evitare memory leak dai controller.
 */
public class InventoryService {

    private static InventoryService instance;
    private final Map<String, Image> ownedItems = new HashMap<>();

    // CopyOnWriteArrayList va bene per thread-safety e per evitare ConcurrentModificationException,
    // MA non bisogna usare iterator.remove() su di essa.
    private final List<WeakReference<InventoryRefreshable>> listeners = new CopyOnWriteArrayList<>();

    private InventoryService() {}

    public static InventoryService getInstance() {
        if (instance == null) instance = new InventoryService();
        return instance;
    }

    public synchronized void addItem(String id, Image image) {
        ownedItems.put(id, image);
        // notifica i listener (su JavaFX Application Thread)
        notifyListeners();
    }

    public synchronized Image getItemImage(String id) {
        return ownedItems.get(id);
    }

    public synchronized boolean hasItem(String id) {
        return ownedItems.containsKey(id);
    }

    /**
     * Registra un listener. Appena registrato, gli viene inviato un refresh (su UI thread).
     */
    public void addListener(InventoryRefreshable l) {
        cleanupListeners();
        // evita doppie registrazioni semplicemente aggiungendo: se vuoi
        // prevenire duplicati, potresti prima controllare l'esistenza.
        listeners.add(new WeakReference<>(l));

        // invoca refresh iniziale sul JavaFX thread
        Platform.runLater(() -> {
            try {
                l.refreshFromInventory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void removeListener(InventoryRefreshable l) {
        // rimuove tutti i riferimenti che sono null o uguali a l
        listeners.removeIf(ref -> {
            InventoryRefreshable v = ref.get();
            return v == null || v == l;
        });
    }

    /**
     * Rimuove i riferimenti raccolti (referenze weak già GCate).
     * Usa removeIf: sicuro su CopyOnWriteArrayList.
     */
    private void cleanupListeners() {
        listeners.removeIf(ref -> ref.get() == null);
    }

    /**
     * Notifica tutti i listener attivi (su JavaFX thread). Prima pulisce i riferimenti nulli.
     */
    private void notifyListeners() {
        Platform.runLater(() -> {
            // prima pulisci eventuali riferimenti nulli
            listeners.removeIf(ref -> ref.get() == null);

            // poi notificali: iterazione diretta è sicura su CopyOnWriteArrayList
            for (WeakReference<InventoryRefreshable> ref : listeners) {
                InventoryRefreshable l = ref.get();
                if (l != null) {
                    try {
                        l.refreshFromInventory();
                    } catch (Exception e) {
                        // proteggiti da listener che possono lanciare eccezioni
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
