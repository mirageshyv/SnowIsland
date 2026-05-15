package com.example.snowisland.entity;

import javax.persistence.*;

@Entity
@Table(name = "warehouse_config")
public class WarehouseConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "warehouse_key", nullable = false, unique = true, length = 50)
    private String warehouseKey;

    @Column(name = "warehouse_name", nullable = false, length = 50)
    private String warehouseName;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(name = "key_item_id", nullable = false)
    private Integer keyItemId;

    @Column(length = 10)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public WarehouseConfig() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getWarehouseKey() { return warehouseKey; }
    public void setWarehouseKey(String warehouseKey) { this.warehouseKey = warehouseKey; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Integer getKeyItemId() { return keyItemId; }
    public void setKeyItemId(Integer keyItemId) { this.keyItemId = keyItemId; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
