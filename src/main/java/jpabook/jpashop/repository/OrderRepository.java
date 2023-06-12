package jpabook.jpashop.repository;


import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;


//[ '주문 리포지토리 개발'강 ]

@RequiredArgsConstructor
@Repository
public class OrderRepository {

    private final EntityManager em;
    /*
    EntityManager는 JPA의 중심적인 인터페이스입니다. 이 객체를 사용해서 데이터베이스와 상호 작용할 수 있습니다.
    EntityManager는 엔티티를 저장, 수정, 삭제, 조회 등의 역할을 담당하고, 데이터베이스랑 통신하며,
    영속성 컨텍스트를 관리하는 등의 역할을 합니다. 예를 들어, 개별적인 데이터를 데이터베이스에 저장하거나,
    데이터베이스 조회 결과를 Java 객체로 변환(Materialization)하는 등의 작업을 담당
     */


//=================================================================================================================


    //< 신규주문 저장 >
    public void save(Order order) {
        em.persist(order);
    }


//==================================================================================================================


    //< '개별 주문(1건)을 DB에서 '해당 주문의 id값'으로 조회'하기 >
    //'클라이언트로부터 매개변수로 들어온 id에 해당하는 1개의 주문'을 DB에서 찾아와서 '그 주문을 리턴'해줌
    public Order findOne(Long orderId) { //'여기서의 매개변수 orderId'는 그냥 여기 메소드에서만 통용되는 것에 불과하고,
                                         //중요한 것은, '레펏 OrderRepository의 메소드 findOne을 호출할 때는',
                                         //반드시 '그 매개변수로 Long 타입'을 넣어주어야 하는 것이다!

        Order order = em.find(Order.class, orderId);

        return order;
    }


//==================================================================================================================


    //[ '주문 리포지토리 개발'강. 01:00~ ]. 코드 pdf p63
    //< 전체 주문 조회 >
    public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o join o.member m" + " WHERE o.status = :status "
                        + " and m.name like :name ", Order.class)
                //1.'select o from Order o': 'Oder 엔티티의 모든 필드들'과
                //2.'join o.member m': Order 객체와 연관관계 매핑되어 있는 Member객체를 서로 '내부 조인'시킨다.
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000) //페이징을 하는데, 최대 1000건만 가져오는 것으로 제한 걸기.
                .getResultList();
    }


//==================================================================================================================


    //[ JPQL 강의 부분임 ].

    //< N+1 문제를 해결하지 못한 JPQL 쿼리문 >

    public List<Order> findAllByString(OrderSearch orderSearch) {

        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //'주문 상태 검색'
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }


        //'회원 이름 검색'
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //􀭭􀘀 1000􀑤
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }


//==================================================================================================================


    //[ '간단한 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화'강. 01:10~ ]. 실전! 스프링 부트와 JPA 활용2 - API 개발과 성능 최적화


    //< N+1 문제를 해결한 JPQL 쿼리문 >
    public List<Order> findAllWithMemberDelivery() {

        //- 아래 쿼리 한 번으로 '주문 Order 객체', '회원 Member 객체', '배송 Delivery 객체'를 각각 조회하는 대신
        //  '한 번의 쿼리로 조회'가 가능해짐. 성능 향상.
        //  결과적으로, 아래 한 번의 쿼리를 통해 '주문', '회원', '배송' 데이터를 '함께 조회(가져옴)'한 후,
        //  '각 정보를 모두 포함하는 List<Order> 객체'를 반환함. 이렇게 함으로써 N+1과 같은 성능 저하 이슈 방지 가능.
        //- 'em.createQuery': 이것을 선언해줌으로써, 'JPQL 쿼리'를 생성해주는 것이 가능함.
        //- 'select o from Order o': '주문 엔티티 Order 객체'를 '조회'하기 위한 쿼리.
        //                          '객체'를 가져오는 것이므로, '별칭(alias) o'를 사용하여, '별칭 o'에 '쿼리로 가져온 결과를 저장'함.
        //- ' join fetch o.member m': '주문 엔티티 Order 객체'와 '회원 엔티티 Member 객체'를 '조인'하고,
        //                            그리고 '회원 엔티티 Member 객체의 정보'를 '가져오기'위한 '조인(fetch join)'임.
        //- ' join fetch o.delivery d': '주문 엔티티 Order 객체'와 '배송 엔티티 Delivery 객체'를 '조인'하고,
        //                              그리고, '배송 엔티티 Delivery 객체의 정보'를 '가져오기'위한 '조인(fetch join)'임.
        //- '.getResultList()': 'em.createQuery(...)'가 다 실행되고 나면, 그 결과로 반환된 TypedQuery 객체에 대해
        //                      '.getResultList()'를 호출하여 결과를 반환함.
        //                      '메소드 getResult()'는 '쿼리를 실행한 후 결과를 List<Order> 형태로 반환'함.
        //- 'Order.class': '메소드 createQuery의 두 번째 파라미터'로, JPQL 쿼리를 통해 반환되는 결과를 어떤 객체 타입'으로
        //                  반환할지 지정하는 역할임.

        //< 'join fetch >
        //*****중요중요!!*** 아주아주 자주 사용함! 100% 이해해야 함! N+1 문제를 해결하는 방법임.
        //- 'join fetch'를 통해 '객체 그래프'와 'select 조회 데이터'를 한 방에 동시에 같이 가져오는 것임!
        //   fetch join'을 아주 적극적으로 활용해야 함!
        //- 여기서 'fetch join'을 사용하였기에 'order -> member'와 'order -> delivery'는 '이미 조회 완료'된 상태이므로,
        //  '주문 엔티티 Order 객체' 내부의 '필드 @ManyToOne(fetch=LAZY) Member'와 '필드 @OneToOne(fetch=LAZY)의
        //  'LAZY'는 이제 무시되고, 따라서 당연히 지연로딩은 없다!

        //-'메소드 createQuery'는 '첫 번째 매개변수'로 'JPQL 쿼리를 사용하여 조회할 대상 및 연관관계를 정의'하고
        // '두 번재 매개변수'로 'JPQL 쿼리의 반환 결과를 어떤 객체 타입으로 반환할지를 지정함'.

        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }



//==================================================================================================================



}
