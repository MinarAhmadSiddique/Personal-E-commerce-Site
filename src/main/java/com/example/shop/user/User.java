package com.example.shop.user;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(name="password_hash",nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length=20)
    private Role role = Role.USER;

    @Column(name="created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt==null){
            createdAt=Instant.now();
        }
    }

    protected User(){

    }

    public User(String email,String passwordHash,String name){
        this.email = email;
        this.passwordHash=passwordHash;
        this.name=name;
    }

    public Long getId(){return id;}

    public String getEmail(){return  email;}
    public void setEmail(String email){this.email=email;}

    public String getPasswordHash(){return passwordHash;}
    public  void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}

    public String getName(){return  name;}
    public void setName(String name){this.name=name;}

    public Role getRole(){return role;}
    public void setRole(Role role){this.role=role;}

    public Instant getCreatedAt(){return createdAt;}
}
