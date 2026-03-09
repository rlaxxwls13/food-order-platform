package nbcamp.food_order_platform.store.domain.repository;

import nbcamp.food_order_platform.store.domain.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    @Query(value = "SELECT * FROM p_store WHERE store_id = :id", nativeQuery = true)
    Optional<Store> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query(value = "SELECT * FROM p_store", nativeQuery = true)
    List<Store> findAllIncludingDeleted();

    @EntityGraph(attributePaths = {"storeRegion", "storeRegion.regionCode", "storeCategories", "storeCategories.category"})
    @Query(
            value = """
                    select distinct s
                    from Store s
                    join s.storeRegion sr
                    join sr.regionCode rc
                    left join s.storeCategories sc
                    left join sc.category c
                    where (:regionCode is null or rc.id = :regionCode)
                      and (:categoryId is null or c.id = :categoryId)
                      and (:storeName is null or :storeName = '' or lower(s.name) like lower(concat('%', :storeName, '%')))
                    """,
            countQuery = """
                    select count(distinct s)
                    from Store s
                    join s.storeRegion sr
                    join sr.regionCode rc
                    left join s.storeCategories sc
                    left join sc.category c
                    where (:regionCode is null or rc.id = :regionCode)
                      and (:categoryId is null or c.id = :categoryId)
                      and (:storeName is null or :storeName = '' or lower(s.name) like lower(concat('%', :storeName, '%')))
                    """
    )
    Page<Store> searchStores(UUID regionCode, UUID categoryId, String storeName, Pageable pageable);

    @EntityGraph(attributePaths = {"storeRegion", "storeRegion.regionCode", "storeCategories", "storeCategories.category"})
    @Query(
            value = """
                    select distinct s
                    from Store s
                    join s.storeRegion sr
                    join sr.regionCode rc
                    left join s.storeCategories sc
                    left join sc.category c
                    where (:regionCode is null or rc.id = :regionCode)
                      and (:categoryId is null or c.id = :categoryId)
                      and (:storeName is null or :storeName = '' or lower(s.name) like lower(concat('%', :storeName, '%')))
                    """,
            countQuery = """
                    select count(distinct s)
                    from Store s
                    join s.storeRegion sr
                    join sr.regionCode rc
                    left join s.storeCategories sc
                    left join sc.category c
                    where (:regionCode is null or rc.id = :regionCode)
                      and (:categoryId is null or c.id = :categoryId)
                      and (:storeName is null or :storeName = '' or lower(s.name) like lower(concat('%', :storeName, '%')))
                    """
    )
    Page<Store> searchAdminStores(
            @Param("regionCode") UUID regionCode,
            @Param("categoryId") UUID categoryId,
            @Param("storeName") String storeName,
            Pageable pageable
    );

    @Query(
            value = """
            select s.store_id
            from p_store s
            join p_store_region sr on sr.store_id = s.store_id
            join p_region_code rc on rc.region_code = sr.region_code
            where (:regionCode is null or rc.region_code = :regionCode)
              and (
                    :categoryId is null
                    or exists (
                        select 1
                        from p_store_category sc
                        where sc.store_id = s.store_id
                          and sc.category_id = :categoryId
                    )
              )
              and (
                    :storeName is null
                    or :storeName = ''
                    or lower(s.name) like lower(concat('%', :storeName, '%'))
              )
            order by s.created_at desc
            """,
            countQuery = """
            select count(*)
            from (
                select distinct s.store_id
                from p_store s
                join p_store_region sr on sr.store_id = s.store_id
                join p_region_code rc on rc.region_code = sr.region_code
                where (:regionCode is null or rc.region_code = :regionCode)
                  and (
                        :categoryId is null
                        or exists (
                            select 1
                            from p_store_category sc
                            where sc.store_id = s.store_id
                              and sc.category_id = :categoryId
                        )
                  )
                  and (
                        :storeName is null
                        or :storeName = ''
                        or lower(s.name) like lower(concat('%', :storeName, '%'))
                  )
            ) t
            """,
            nativeQuery = true
    )
    Page<UUID> searchAdminStoreIdsIncludingDeleted(
            UUID regionCode,
            UUID categoryId,
            String storeName,
            Pageable pageable
    );

    @Query(
            value = """
        select *
        from p_store
        where store_id in (:storeIds)
        """,
            nativeQuery = true
    )
    List<Store> findAllByIdInIncludingDeleted(@Param("storeIds") List<UUID> storeIds);
}
