package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

import javax.persistence.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name",nullable = false)
    private String name;
    private String description;
    @Column(name = "available",nullable = false)
    private Boolean available;
    @ManyToOne
    @JoinColumn(name = "owner_id",referencedColumnName = "id")
    private User owner;
    private Long requestId;
}
