package ru.coffee.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import ru.coffee.domain.CoffeeOrder;
import ru.coffee.domain.CoffeeOrderItem;
import ru.coffee.domain.CoffeeType;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class CoffeeDAOHibernate implements CoffeeDAO{

    @Autowired
    private SessionFactory sessionFactory;
            
    @Override
    public void calculateCost(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        //this method uses coffeeDao because there is not sense to use hibernate for it
        CoffeeJDBCUtils.calculateCost(sessionFactory.getCurrentSession().connection(), order, orderItems);
    }

    @Override
    public void createOrder(CoffeeOrder order, List<CoffeeOrderItem> orderItems) throws SQLException {
        order.setId(getNextID("coffeeorder"));
        for(CoffeeOrderItem orderItem: orderItems){
            orderItem.setId(getNextID("coffeeorderitem"));
            orderItem.setCoffeeOrder(order);
            sessionFactory.getCurrentSession().save(orderItem);
        }
    }

    @Override
    public CoffeeType getCoffeeTypeById(long id) throws SQLException {
        final String hql = "from CoffeeType where id=:id";
        return (CoffeeType)sessionFactory.getCurrentSession().
                createQuery(hql).setLong("id", id).uniqueResult();
    }

    @Override
    public long getNextID(String tableName) throws SQLException {
        final String className = getClassNameByTable(tableName);
        final String hql = String.format("SELECT MAX(id) FROM %s", className);
        return (long)sessionFactory.getCurrentSession().createQuery(hql).uniqueResult() + 1;
    }

    @Override
    public CoffeeOrder getOrder(long id, List<CoffeeOrderItem> orderItems) throws SQLException, ParseException {
        CoffeeOrder coffeeOrder = (CoffeeOrder)sessionFactory.getCurrentSession().
                createCriteria(CoffeeOrder.class).add(Restrictions.eq("id", id)).uniqueResult();
        
        orderItems.clear();
        Criteria criteria = sessionFactory.getCurrentSession().
                createCriteria(CoffeeOrderItem.class).add(Restrictions.eq("coffeeOrder", coffeeOrder));
        orderItems.addAll(criteria.list());

        return coffeeOrder;
    }

    @Override
    public List<CoffeeType> listCoffeeType(Locale locale, boolean withDisabled) throws SQLException {
        String sql = CoffeeJDBCUtils.getSqlQueryCoffeeType(locale, withDisabled);
        return (List<CoffeeType>)sessionFactory.getCurrentSession().createSQLQuery(sql).
                addEntity(CoffeeType.class).list();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private String getClassNameByTable(String tableName) {
        switch (tableName) {
            case "coffeeorder":     return CoffeeOrder.class.getSimpleName();
            case "coffeeorderitem": return CoffeeOrderItem.class.getSimpleName();
        }
        return null;
    }
}
