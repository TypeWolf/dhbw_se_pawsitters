package com.dhbw.pawsitters.service;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.GenericRepository;
import com.dhbw.pawsitters.repository.GenericRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UnitOfWork {

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<Class<?>, GenericRepository<?, ?>> repositories = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T, ID> GenericRepository<T, ID> getRepository(Class<T> entityClass) {
        return (GenericRepository<T, ID>) repositories.computeIfAbsent(entityClass, 
            clazz -> new GenericRepositoryImpl<>(entityManager, clazz));
    }

    // Generic CRUD methods
    public <T, ID> T save(T entity) {
        return getRepository((Class<T>) entity.getClass()).save(entity);
    }

    public <T, ID> T getById(Class<T> entityClass, ID id) {
        return getRepository(entityClass).findById(id)
                .orElseThrow(() -> new RuntimeException(entityClass.getSimpleName() + " not found with id: " + id));
    }

    public <T> List<T> getAll(Class<T> entityClass) {
        return getRepository(entityClass).findAll();
    }

    public <T, ID> void delete(Class<T> entityClass, ID id) {
        getRepository(entityClass).deleteById(id);
    }

    public <T, V> List<T> getByProperty(Class<T> entityClass, String propertyName, V value) {
        String jpql = String.format("SELECT e FROM %s e WHERE e.%s = :value", entityClass.getSimpleName(), propertyName);
        return entityManager.createQuery(jpql, entityClass)
                .setParameter("value", value)
                .getResultList();
    }

    // Specialized but still generic method: Get all by user/owner
    // This assumes the entity has a field that maps to AppUser (like 'owner' or 'requester')
    public <T> List<T> getAllByUser(Class<T> entityClass, AppUser user, String fieldName) {
        return getByProperty(entityClass, fieldName, user);
    }

    // Convenience methods for specific entities but using the same generic engine
    public List<Pet> getPetsByOwner(AppUser owner) {
        return getAllByUser(Pet.class, owner, "owner");
    }

    public List<SittingRequest> getRequestsByUser(AppUser user) {
        return getAllByUser(SittingRequest.class, user, "requester");
    }
}
