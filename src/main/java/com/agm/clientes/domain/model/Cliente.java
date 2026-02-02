package com.agm.clientes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("clientes")
public class Cliente {

    @Id
    private UUID id;

    @Column("nombre")
    private String nombre;

    @Column("email")
    private String email;

    @Column("telefono")
    private String telefono;

    @Column("activo")
    private Boolean activo;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column("version")
    private Long version;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.activo = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.activo = false;
        this.updatedAt = LocalDateTime.now();
    }
}
