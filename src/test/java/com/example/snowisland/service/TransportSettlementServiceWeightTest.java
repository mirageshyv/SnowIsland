package com.example.snowisland.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransportSettlementServiceWeightTest {

    @Test
    public void freePlayerWarehouseIs50ForTownAndIsland() {
        TransportSettlementService svc = new TransportSettlementService();
        TransportSettlementService.TransportPlan plan = new TransportSettlementService.TransportPlan();
        plan.tier = "free";
        plan.mode = "player_to_warehouse";

        plan.destWarehouse = "general";
        assertEquals(50, svc.resolveMaxWeight(plan, null));

        plan.destWarehouse = "fuel";
        assertEquals(50, svc.resolveMaxWeight(plan, null));

        plan.mode = "warehouse_to_player";
        plan.sourceWarehouse = "general";
        plan.destWarehouse = null;
        assertEquals(50, svc.resolveMaxWeight(plan, null));
    }

    @Test
    public void warehouseLinkFreeCapsUnchanged() {
        TransportSettlementService svc = new TransportSettlementService();
        TransportSettlementService.TransportPlan plan = new TransportSettlementService.TransportPlan();
        plan.tier = "free";
        plan.mode = "warehouse_to_warehouse";
        plan.sourceWarehouse = "fuel";
        plan.destWarehouse = "armory";
        assertEquals(100, svc.resolveMaxWeight(plan, null));

        plan.destWarehouse = "general";
        assertEquals(50, svc.resolveMaxWeight(plan, null));
    }
}
