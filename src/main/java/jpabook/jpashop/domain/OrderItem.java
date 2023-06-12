package jpabook.jpashop.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.Item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED) //아래 중간 즈음에 있는 'protected OrderItem(){}' 부분을 대신해서 붙여줌.
@Getter
@Setter
@Entity
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "ORDER_ITEM_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    //[ '간단한 주문 조회 V1: 엔티티를 직접 노출'강. 07:54~ ]. 실전! 스프링 부트와 JPA 활용2 - API 개발과 성능 최적화
    @JsonIgnore //- 1)만약, 정말 어쩔 수 없이 DTO가 아닌 '엔티티'를 생으로 노출시켜 데이터를 송수신해야 하는 경우일 때,
                //  2)그리고, 그 경우가 'N:1 또는 1:1 '양'방향 매핑'일 때에는,
                //  => 1.두 연관관계 엔티티들 중 하나는 반드시 @JsonIgnore 해주고, 그리고
                //     2.'서버 실행 JpashopApplication'의 내부에 'Hibernate5Module 객체' 관련 로직을 작성해줘야 한다!
                //       ('Hibernate5Module 라이브러리' 의존성을 build.gradle에 추가해줘야 함)
                //  이렇게 함을 통해서 둘 중의 하나를 @JsonIgnore로 끊어줘야, 무한루프 문제가 발생하지 않음!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    private int orderPrice; //고객이 주문하는 당시의 가격

    private int count; //주문 수량

//==================================================================================================================


    //[ '주문 서비스 개발'강. 09:10~ ]

    //''서비스 OrderService 내부'의 '< 신규주문 저장 > 내부'의 '3.주문상품 생성' 부분 참조'
    //'protected OrderItem(){}'는 '클래스 OrderItem의 맨 위에 클래스 어노테이션
    //@NoAragsConstructor(access = AccessLevel.PROTECTED'를 붙이면 동일한 기능이 되게 된다.
//    protected OrderItem(){
//
//    }


//==================================================================================================================


    //[ '주문, 주문상품 엔티티 개발'강. 13:15~ ]

    //< '신규 주문상품 생성'하는 '팩토리 메소드' >: '팩토리 메소드 패턴'이다!!!

    //- '사용자 생성자'와 정확히 동일한 역할을 함.
    //  '생성자를 사용하지 않고' 이렇게 '생성하는 메소드를 따로 쓴 이유'는, 다른 클래스에서 '새로운 OrderItem 객체를 생성할 때'
    //  '기본 생성자와 달리 내 임의로 내가 원하는 이름과 속성(?)을 줄 수 있기 때문'임.
    //  실제로 이 경우처럼 '명확함'을 위해 '생성자 역할을 하지만, 그 고유한 이름을 가지는 메소드'를 만들어 '객체를 생성하는 역할'을
    //  하는 경우도 많이 있다!
    //- *****중요*****
    // 다만, 그럼에도 '팩토리 메소드'는 '생성자가 아니기 때문'에,
    //'현재 클래스 OrderItem의 기본생성자'는 여전히 숨겨진 상태로 생략되어 존재하는 것이 맞다!(당연..)

    //< '팩토리 메소드' >
    //- '현재 주문상품 객체 OrderItem'의 '객체 생성을 전담'하는 디자인패턴.
    //  외부 클래스에서 '현재 팩토리메소드 createOrderItem을 호출'하여, '새로운 내가 원하는 속성(필드)만 넣은 주문상품 OrderItem 객체'를
    //  생성할 수 있음.
    //- '현재 팩토리메소드 createOrderItem'은 '정적(static) 메소드'이기 때문에, 외부에서 '주문상품 객체 OrderItem'을
    //  '클참뉴클'로 생성하는 단계 필요 없이, 그냥 바로 '현재 팩토리메소드 createOrderItem'을 호출하여,
    //  '새로운 주문상품 객체 OrderItem'을 생성할 수 있다.
    //  (당연히, 원래는 어떤 클래스의 외부에서 그 클래스의 객체를 생성하려면, 당연히 먼저 그 클래스의 클참뉴클을 해줘야 하는 것은 맞음)

    //- 유연성: 객체 생성 로직을 변경하거나 대체할 수 있음. 팩토리메소드를 구현한 서브클래스를 통해 다양한 생성 방식을 지원할 수 있음.
    //- 캡슐화: 객체 생성 로직이 한 곳에 집중되어 있어, 코드의 가독성과 유지보수성을 높일 수 있음.
    //- 객체 생성 과정이 복잡하고 다양한 조건에 따라 달라질 때
    //  : 팩토리 메소드를 통해 객체 생성 로직을 서브클래스에 위임하여 복잡성을 관리할 수 있음.
    //- 객체 생성 방식의 유연성이 필요한 경우:
    //  : 객체 생성 방식을 변경하거나 확장해야 할 때 팩토리 메소드 패턴을 사용할 수 있음.
    //-
    //- '현재 주문상품 객체 OrderItem'의 '객체 생성 로직'을 변경해야 할 때, 전체 코드에 변경이 필요하지 않기 때문에,
    //  수정 사항에서 오는 영향을 최소화할 수 있음.
    //-


    //< '팩토리 메소드를 사용한 현재 객체 생성'과, '생성자를 사용한 현재 객체 생성' 의 차이 >
    //- '팩토리 메소드를 사용한 현재 객체 생성'은 '생성자의 역할인 객체 초기화 작업 이외'에도, 추가 작업을 수행할 수 잇음.
    //-
    //  '정적(static) 메소드'이기 때문에, 외부에서 현재 주문상품 객체를 생성하지 않아도(=OrderItem orderItem = new OrderItem();)
    //  바로 아래 '팩토리 메소드 createOrderItem을 호출 가능'하며,
    //  개발자가 팩토리메소드의 동작을 더 잘 이해하도록 팩토리메소드의 이름을 직관적인 이름으로 만들 수 있음.
    //  또한, '리턴 타입'도 반드시 지정해야 함('생성자'는 당연히 리턴타입 없음).

    //- '생성자를 사용한 현재 객체 생성'은 객체의 초기화를 담당하며, '객체가 생성될 때 자동으로 호출됨'.
    //  생성자는 클래스명과 동일한 이름을 지니며, 리턴 타입을 지정하지 않음.
    //  즉, 생성자는 '객체 초기화 작업'만 수행함.


    //< 'static 선언' >
    //- 아래 메소드는 'static'으로 선언되어 있어서, 아래 메소드는 '클래스 OrderItem'의 인스턴스를 생성하지 않아도 호출할 수 있음.

    public static OrderItem createOrderItem(Item item, int orderPrice, int count){ //- 'Item': 어떤 상품을
                                                                                   //- 'orderPrice': 얼마의 가격에
                                                                                   //- 'count': 몇 개나 샀는지

        //- 팩토리 메소드를 사용함으로써, 다른 클래스에서 '새로운 주문상품을 생성할 때',
        //  그 클래스에서만 원하는 '해당 주문상품의 특정 속성(필드)'만 가진 '새로운 주문상품 객체'를 생성할 수 있음.
        //- 이는 생성자를 사용하지 않고 '팩토리 메소드'를 사용항여 객체를 생성하는 방식으로, 가독성과 명확성을 높일 수 있음.

        //1.'새로운 주문상품 OrderItem 객체'를 생성함.
        OrderItem orderItem = new OrderItem();

        //2.'1번'에서 생성된 '새로운 OrderItem 객체의 필드(속성)들'을 '이 메소드의 인자값 item, orederPrice, count'로 설정함.
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        //3.주문상품을 생성하면(위의 1, 2번 과정), 해당 상품의 재고를 주문한 수량만큼 감소시키는 로직.
        //  즉, 외부에서 현재 이 메소드를 호출하여, 재고를 주문한 수량(=count) 만큼 감소시킴.
        item.removeStock(count); //신규주문이 발생하여, 신규주문품목이 생기면, 당연히 '기존의 재고에서 신규주문 수량만큼 감소'시켜야
                                 //함. 당연..

        //4.'1~2번'을 통해 생성된 '새로운 주문상품 OrderItem 객체'를 반환함.
        //   즉, '새로운 주문상품 OrderItem 객체'를 '외부로 전달하는 역할'임.
        return orderItem;
    }


//==================================================================================================================


    // pdf p57 상단 참조
    //# 아래처럼 '엔티티 객체 내부에 비즈니스로직을 넣는 패턴'을 '도메인 모델 패턴'이라고 함.
    //  반면, '서비스 계층 내부에서 비즈니스로직을 처리하는 패턴'은 '트랜잭션 스크립트 패턴'이라고 함.
    //# 'JPA에서는 도메인 모델 패턴을 많이 활용'하고, 'MyBatis처럼 SQL 쿼리를 직접 사용하는 방법에서는 트랜잭션 스크립트 패턴'을
    //  많이 활용한다!

    //============= 비즈니스로직 =============

    //[ '주문, 주문상품 엔티티 개발'강. 07:00~ ]

    //< 주문상품에 대한 주문취소 >
    //고객이 '하나의 건 주문할 때('Order 객체' 호출)', 그 하나의 건 주문 안에는 'N개의
    //주문상품 OrderItem'이 있을 수 있기 때문에, for문을 돌려서 그 하나의 건 주문 안에 있는
    //'N개의 모든 주문상품들 OrderItem 취소(메소드 cancel)시켜야 함'.
    public void cancel(){

        getItem().addStock(count); //- '기존 주문수량 count'만큼 '기존 재고에 다시 더해줘야 한다!'.
                                   //- '필드 item'과 '메소드 cancel'이 '같은 엔티티 OrderItem의 내부에 동시에 존재'하고 있는 상황에서,
                                   //  '필드 item을 통해 엔티티 Item 객체를 호출할 때'에는, '게터 Item'을 사용한다!
                }


//==================================================================================================================


    //============= 조회 로직 =============

    //[ '주문, 주문상품 엔티티 개발'강. 10:00~ ]

    //< 해당 하나의 주문 건에 속한 총 주문상품들의 총 주문금액 합 조회(=신규주문으로 들어온 전체 상품의 총 가격 조회) >
    public int getTotalPrice(){


        return getCount() * getOrderPrice(); //'현재 같은 클래스 OrderItem 내부의 필드 count와 필드 orderPrice'를
                                             //여기서 사용함에도 불구하고, 그 필드(데이터)를 직접 사용하는 것이 아니라,
                                             //그것들의 '게터 getter'를 사용해야 한다!
    }


//==================================================================================================================







}
