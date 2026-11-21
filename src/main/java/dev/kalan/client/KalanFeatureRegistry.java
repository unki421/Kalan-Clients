package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanFeatureRegistry {

    private static final List<KalanFeature> FEATURES = new ArrayList<>();

    private KalanFeatureRegistry() {
    }

    public static void register(KalanFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }

        if (find(feature.getName()) != null) {
            throw new IllegalArgumentException(
                    "Feature already registered: " + feature.getName()
            );
        }

        FEATURES.add(feature);
    }

    public static KalanFeature find(String name) {
        if (name == null) {
            return null;
        }

        for (KalanFeature feature : FEATURES) {
            if (feature.getName().equalsIgnoreCase(name)) {
                return feature;
            }
        }

        return null;
    }

    public static List<KalanFeature> getAll() {
        return Collections.unmodifiableList(FEATURES);
    }

    public static List<KalanFeature> getByCategory(KalanCategory category) {
        List<KalanFeature> result = new ArrayList<>();

        for (KalanFeature feature : FEATURES) {
            if (feature.getCategory() == category) {
                result.add(feature);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public static int size() {
        return FEATURES.size();
    }
}
