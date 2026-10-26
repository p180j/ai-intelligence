package com.aiintelligence.report;

import com.aiintelligence.common.NotFoundException;
import com.aiintelligence.event.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportService {
    private static final List<EventCategory> SECTIONS=Arrays.asList(EventCategory.MUST_READ,EventCategory.TRY_NOW,EventCategory.BUSINESS,EventCategory.FRONTIER);
    private final DailyReportRepository repository; private final EventService events;private final String publicBaseUrl;
    public DailyReportService(DailyReportRepository repository,EventService events,@org.springframework.beans.factory.annotation.Value("${app.public-base-url:http://localhost:8080}")String publicBaseUrl){this.repository=repository;this.events=events;this.publicBaseUrl=publicBaseUrl.replaceAll("/$","");}
    public List<DailyReport> findAll(){List<DailyReport> list=repository.findAll();list.forEach(this::load);return list;}
    public DailyReport findById(long id){DailyReport r=repository.findById(id).orElseThrow(()->new NotFoundException("Daily report not found: "+id));return load(r);}
    public DailyReport latestPublished(){LocalDate today=LocalDate.now();DailyReport report=new DailyReport();report.reportDate=today;report.title="最新 AI 情报";report.summary="持续更新已经审核发布、值得关注的 AI 动态。";report.status=DailyReportStatus.PUBLISHED;report.events=new ArrayList<>();int order=1;for(Event event:repository.findAllPublished()){DailyReportEvent item=new DailyReportEvent();item.eventId=event.id;item.section=(event.category==EventCategory.NORMAL?EventCategory.FRONTIER:event.category).name();item.displayOrder=order++;item.event=events.findById(event.id);report.events.add(item);}return report;}
    @Transactional public DailyReport generate(LocalDate date){Optional<DailyReport> existing=repository.findByDate(date);long id;if(existing.isPresent()){DailyReport report=findById(existing.get().id);if(report.status!=DailyReportStatus.DRAFT)return report;id=report.id;}else{String title="AI 前沿日报｜"+date.getMonthValue()+"月"+date.getDayOfMonth()+"日";id=repository.insert(date,title,"今天真正值得关注的 AI 变化。");}DailyReport report=findById(id);int order=report.events.stream().mapToInt(item->item.displayOrder).max().orElse(0)+1;for(Event e:repository.candidates(date))repository.addEvent(id,e.id,e.category.name(),order++);return findById(id);}
    @Transactional public DailyReport update(long id,DailyReportUpdateRequest r){DailyReport current=findById(id);ensureDraft(current);repository.update(id,r.title,r.summary);return findById(id);}
    @Transactional public DailyReport add(long id,ReportEventRequest r){DailyReport report=findById(id);ensureDraft(report);Event e=events.findById(r.eventId);if(e.reviewStatus!=ReviewStatus.APPROVED)throw new IllegalArgumentException("Only APPROVED event can enter report");EventCategory section=EventCategory.valueOf(r.section);if(!SECTIONS.contains(section))throw new IllegalArgumentException("Unsupported report section: "+r.section);repository.addEvent(id,r.eventId,section.name(),r.displayOrder);return findById(id);}
    @Transactional public DailyReport remove(long id,long eventId){ensureDraft(findById(id));repository.removeEvent(id,eventId);return findById(id);}
    @Transactional public DailyReport publish(long id){DailyReport report=findById(id);ensureDraft(report);repository.publish(id);for(DailyReportEvent item:report.events)events.publish(item.eventId);return findById(id);}
    @Transactional public DailyReport offline(long id){findById(id);repository.offline(id);return findById(id);}
    @Transactional public DailyReport feedback(long id,long eventId,String value){findById(id);com.aiintelligence.quality.ReportQualityResult result=com.aiintelligence.quality.ReportQualityResult.valueOf(value);repository.feedback(id,eventId,result.name());return findById(id);}
    public String wechat(long id){DailyReport r=findById(id);List<DailyReportEvent> included=new ArrayList<>();for(DailyReportEvent item:r.events)if(!"NOT_WORTHY".equals(item.qualityFeedback))included.add(item);String highlights=included.stream().limit(3).map(x->x.event.title).collect(java.util.stream.Collectors.joining("、"));String title="AI 前沿日报｜"+r.reportDate.getMonthValue()+"月"+r.reportDate.getDayOfMonth()+"日"+(highlights.isEmpty()?"":"："+highlights);StringBuilder b=new StringBuilder("标题：\n").append(title).append("\n\n摘要：\n今天值得关注的有 ").append(included.size()).append(" 件事。\n");String last=null;int number=1;for(DailyReportEvent item:included){if(!item.section.equals(last)){last=item.section;b.append("\n").append(icon(last)).append(" ").append(label(last)).append("\n\n");}Event e=item.event;b.append(number++).append(". ").append(e.title).append("\n\n").append(shorten(e.summary,120)).append("\n\n👉 查看完整内容\n").append(publicBaseUrl).append("/event/").append(e.id).append("\n\n---\n");}return b.toString();}
    private DailyReport load(DailyReport r){r.events=repository.findEvents(r.id);return r;}
    private DailyReport loadPublished(DailyReport r){r.events=repository.findPublishedEvents(r.id);return r;}
    private void ensureDraft(DailyReport r){if(r.status!=DailyReportStatus.DRAFT)throw new IllegalArgumentException("Only DRAFT report can be edited");}
    private String icon(String s){return "MUST_READ".equals(s)?"🔥":"TRY_NOW".equals(s)?"🛠":"BUSINESS".equals(s)?"💰":"🧪";}
    private String label(String s){return "MUST_READ".equals(s)?"必看":"TRY_NOW".equals(s)?"值得试":"BUSINESS".equals(s)?"商业机会":"前沿观察";}
    private String shorten(String value,int max){if(value==null)return "";return value.length()<=max?value:value.substring(0,max)+"…";}
}
