/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.listeners;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.CostNormsGeneratorFields;
import com.qcadoo.mes.materialFlowResources.constants.CostNormsLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.ProductsToUpdate;
import com.qcadoo.mes.materialFlowResources.costNorms.CostNormsService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CostNormsGeneratorListeners {

    @Autowired
    private CostNormsService costNormsService;

    public void updateCostNorms(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent generatorForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity generator = generatorForm.getPersistedEntityWithIncludedFormValues();

        String costsSource = generator.getStringField(CostNormsGeneratorFields.COSTS_SOURCE);

        boolean allProducts = ProductsToUpdate.of(generator).compareTo(ProductsToUpdate.ALL) == 0;

        List<Entity> products = Lists.newArrayList();

        if (!allProducts) {
            products = generator.getHasManyField(CostNormsGeneratorFields.PRODUCTS);

            if (products.isEmpty()) {
                view.addMessage("materialFlowResources.info.costNormsNotUpdated", ComponentState.MessageType.INFO);

                return;
            }
        }

        List<Entity> warehouses = Lists.newArrayList();

        if ("01mes".equals(costsSource)) {
            warehouses = generator.getHasManyField(CostNormsGeneratorFields.WAREHOUSES).stream()
                    .map(warehouse -> warehouse.getBelongsToField(CostNormsLocationFields.LOCATION)).collect(Collectors.toList());
        }

        costNormsService.updateCostNormsForProductsFromWarehouses(products, warehouses);

        view.addMessage("materialFlowResources.success.costNormsUpdated", ComponentState.MessageType.SUCCESS);
    }

    public void toggleProductsGrid(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent generatorForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(CostNormsGeneratorFields.PRODUCTS);

        Entity generator = generatorForm.getPersistedEntityWithIncludedFormValues();

        boolean gridVisible = ProductsToUpdate.of(generator).compareTo(ProductsToUpdate.SELECTED) == 0;

        productsGrid.setVisible(gridVisible);

        if (!gridVisible) {
            generator.setField(CostNormsGeneratorFields.PRODUCTS, null);

            generatorForm.setEntity(generator);
        }
    }

}
