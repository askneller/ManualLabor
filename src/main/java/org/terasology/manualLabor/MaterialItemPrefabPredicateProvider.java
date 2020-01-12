/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.manualLabor;

import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.naming.Name;
import org.terasology.substanceMatters.components.MaterialCompositionComponent;
import org.terasology.substanceMatters.components.MaterialItemComponent;
import org.terasology.workstation.process.inventory.ItemPrefabPredicateFactory;
import org.terasology.workstation.process.inventory.ItemPrefabPredicateProvider;

@RegisterSystem
public class MaterialItemPrefabPredicateProvider extends BaseComponentSystem implements ItemPrefabPredicateProvider {

    private static final Logger logger = LoggerFactory.getLogger(MaterialItemPrefabPredicateProvider.class);

    @Override
    public void initialise() {
        super.initialise();
        ItemPrefabPredicateFactory.registerProvider(this);
    }

    @Override
    public boolean canProvideForResourceUrn(ResourceUrn urn) {
        Name moduleName = urn.getModuleName();
        Name resourceName = urn.getResourceName();
        return "SubstanceMatters".equals(moduleName.toString())
                && "MaterialItem".equals(resourceName.toString());
    }

    @Override
    public Predicate<EntityRef> provide(ResourceUrn urn) {
        return new MaterialItemPrefabPredicate(urn);
    }

    private static final class MaterialItemPrefabPredicate implements Predicate<EntityRef> {
        private ResourceUrn prefab;

        private MaterialItemPrefabPredicate(ResourceUrn prefab) {
            this.prefab = prefab;
        }

        @Override
        public boolean apply(EntityRef input) {
            // TODO works for my wand but not for ManualLabor:BronzeDust. Curious
            ItemComponent item = input.getComponent(ItemComponent.class);
            if (item == null) {
                return false;
            }

            MaterialItemComponent materialItemComponent = input.getComponent(MaterialItemComponent.class);
            MaterialCompositionComponent materialCompositionComponent =
                    input.getComponent(MaterialCompositionComponent.class);
            if (materialItemComponent == null) {
                return false;
            }
            // Find the prefab and substance
            Name fragmentName = prefab.getFragmentName();
            String[] parts = fragmentName.toString().split("\\|");

            if (parts.length == 2) {
                // There is a prefab and a substance
                String thePrefab = parts[0];
                String substance = parts[1];
                return input.getParentPrefab().getName().equals(thePrefab)
                        && materialCompositionComponent.contents.containsKey(substance);
            }

            return false;
        }
    }
}
