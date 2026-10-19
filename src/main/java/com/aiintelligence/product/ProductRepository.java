package com.aiintelligence.product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;
    public ProductRepository(JdbcTemplate jdbcTemplate){this.jdbcTemplate=jdbcTemplate;}
    public List<Product> findAll(){return jdbcTemplate.query("SELECT * FROM ai_product ORDER BY name",rowMapper());}
    public Optional<Product> findById(long id){return jdbcTemplate.query("SELECT * FROM ai_product WHERE id=?",rowMapper(),id).stream().findFirst();}
    public Optional<Product> findBySlug(String slug){return jdbcTemplate.query("SELECT * FROM ai_product WHERE slug=?",rowMapper(),slug).stream().findFirst();}
    public List<Product> search(String q){String value="%"+(q==null?"":q.toLowerCase())+"%";return jdbcTemplate.query("SELECT * FROM ai_product WHERE LOWER(name) LIKE ? OR LOWER(summary) LIKE ? ORDER BY name LIMIT 50",rowMapper(),value,value);}
    public long insert(ProductRequest r){KeyHolder key=new GeneratedKeyHolder();jdbcTemplate.update(c->{java.sql.PreparedStatement s=c.prepareStatement("INSERT INTO ai_product (name,slug,summary,product_type,company_name,official_url,github_url,docs_url,api_url,open_source,availability,capability_tags) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",new String[]{"id"});bind(s,r);return s;},key);return key.getKey().longValue();}
    public int update(long id,ProductRequest r){return jdbcTemplate.update(c->{java.sql.PreparedStatement s=c.prepareStatement("UPDATE ai_product SET name=?,slug=?,summary=?,product_type=?,company_name=?,official_url=?,github_url=?,docs_url=?,api_url=?,open_source=?,availability=?,capability_tags=?,updated_at=CURRENT_TIMESTAMP WHERE id=?");bind(s,r);s.setLong(13,id);return s;});}
    public void attachEvent(long productId,long eventId,String relationType){int count=jdbcTemplate.update("UPDATE ai_product_event SET relation_type=? WHERE product_id=? AND event_id=?",relationType,productId,eventId);if(count==0)jdbcTemplate.update("INSERT INTO ai_product_event(product_id,event_id,relation_type) VALUES(?,?,?)",productId,eventId,relationType);}
    public List<ProductEvent> findEvents(long productId){return jdbcTemplate.query("SELECT e.id event_id,e.title,e.summary,e.category,e.event_time,pe.relation_type FROM ai_product_event pe JOIN ai_event e ON e.id=pe.event_id WHERE pe.product_id=? ORDER BY COALESCE(e.event_time,e.created_at) DESC,e.id DESC",(rs,n)->{ProductEvent e=new ProductEvent();e.eventId=rs.getLong("event_id");e.title=rs.getString("title");e.summary=rs.getString("summary");e.category=rs.getString("category");e.eventTime=local(rs.getTimestamp("event_time"));e.relationType=rs.getString("relation_type");return e;},productId);}
    public List<ProductEvent> findPublishedEvents(long productId){return jdbcTemplate.query("SELECT e.id event_id,e.title,e.summary,e.category,e.event_time,pe.relation_type FROM ai_product_event pe JOIN ai_event e ON e.id=pe.event_id WHERE pe.product_id=? AND e.review_status='APPROVED' AND e.publish_status='PUBLISHED' ORDER BY COALESCE(e.event_time,e.created_at) DESC,e.id DESC",(rs,n)->{ProductEvent e=new ProductEvent();e.eventId=rs.getLong("event_id");e.title=rs.getString("title");e.summary=rs.getString("summary");e.category=rs.getString("category");e.eventTime=local(rs.getTimestamp("event_time"));e.relationType=rs.getString("relation_type");return e;},productId);}
    private void bind(java.sql.PreparedStatement s,ProductRequest r)throws SQLException{s.setString(1,r.name);s.setString(2,r.slug);s.setString(3,r.summary);s.setString(4,r.productType);s.setString(5,r.companyName);s.setString(6,r.officialUrl);s.setString(7,r.githubUrl);s.setString(8,r.docsUrl);s.setString(9,r.apiUrl);s.setBoolean(10,Boolean.TRUE.equals(r.openSource));s.setString(11,r.availability);s.setString(12,r.capabilityTags);}
    private RowMapper<Product> rowMapper(){return(rs,n)->map(rs);} private Product map(ResultSet rs)throws SQLException{Product p=new Product();p.id=rs.getLong("id");p.name=rs.getString("name");p.slug=rs.getString("slug");p.summary=rs.getString("summary");p.productType=rs.getString("product_type");p.companyName=rs.getString("company_name");p.officialUrl=rs.getString("official_url");p.githubUrl=rs.getString("github_url");p.docsUrl=rs.getString("docs_url");p.apiUrl=rs.getString("api_url");p.openSource=rs.getBoolean("open_source");p.availability=rs.getString("availability");p.capabilityTags=rs.getString("capability_tags");p.createdAt=local(rs.getTimestamp("created_at"));p.updatedAt=local(rs.getTimestamp("updated_at"));return p;} private static LocalDateTime local(Timestamp v){return v==null?null:v.toLocalDateTime();}
}
