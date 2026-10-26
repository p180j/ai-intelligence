const content=document.querySelector('#content');
const esc=value=>String(value??'').replace(/[&<>"']/g,char=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const fmt=value=>value?new Date(value).toLocaleDateString('zh-CN',{year:'numeric',month:'long',day:'numeric'}):'日期待确认';
const dateKey=value=>{const d=new Date(value);return Number.isNaN(d.getTime())?'':`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`};
const get=async url=>{const response=await fetch(url);if(!response.ok)throw new Error('内容暂不可用');return response.status===204?null:response.json()};
const sectionLabels={MUST_READ:'今日必看',TRY_NOW:'值得试',BUSINESS:'商业机会',FRONTIER:'前沿观察',NORMAL:'其他 AI 动态'};
const sectionIds={MUST_READ:'must-read',TRY_NOW:'try-now',BUSINESS:'business',FRONTIER:'frontier',NORMAL:'other'};
const availabilityLabels={AVAILABLE:'已正式可用',BETA:'Beta 测试',PREVIEW:'预览阶段',WAITLIST:'需要申请',RESEARCH_ONLY:'研究阶段',COMING_SOON:'即将推出',DISCONTINUED:'已停止服务'};
const availability=value=>{const raw=String(value||'').trim(),known=availabilityLabels[raw.toUpperCase()];if(known)return known;const lower=raw.toLowerCase();if(lower.startsWith('not available')||lower.startsWith('unavailable'))return '尚未开放';if(lower.startsWith('available'))return '已正式可用';if(lower.includes('private beta')||lower.includes('closed beta'))return '封闭测试';if(lower.includes('beta'))return 'Beta 测试';if(lower.includes('preview'))return '预览阶段';if(lower.includes('waitlist'))return '需要申请';return raw||'可用情况待确认'};
const readingTime=event=>{const text=[event.summary,event.whatHappened,event.whyImportant,event.availability,event.developerImpact].filter(Boolean).join('');return `约 ${Math.max(2,Math.ceil(text.length/350))} 分钟阅读`};
const products=event=>(event.products||[]).map(product=>`<a class="text-link" href="/product/${esc(product.slug)}">${esc(product.name)}</a>`).join('、');
const tags=value=>String(value||'').split(/[,，、]/).map(item=>item.trim()).filter(Boolean);
const externalLink=(url,label)=>url?`<a class="outline-link" target="_blank" rel="noopener" href="${esc(url)}">${label}<span aria-hidden="true"> ↗</span></a>`:'';

const eventCard=event=>{const reader=event.readerArticle,title=reader?.readerTitle||event.title,summary=reader?.readerLead||event.summary;return `<article class="story-card">
  <div class="story-meta"><span>发布于 ${fmt(event.publishedAt||event.eventTime)}</span><span>${readingTime(reader?{summary:reader.readerLead,whatHappened:reader.readerContent,whyImportant:reader.readerConclusion}:event)}</span>${event.availability?`<span>${esc(availability(event.availability))}</span>`:''}</div>
  <h3><a href="/event/${event.id}">${esc(title)}</a></h3>
  <p>${esc(summary||'')}</p>
  ${event.products?.length?`<div class="story-product">相关产品：${products(event)}</div>`:''}
</article>`};

async function home(){
  const report=await get('/api/public/report/latest');
  if(!report){content.innerHTML='<div class="empty">今天的情报正在整理中，稍后再来看看。</div>';return}
  document.title='最新 AI 情报 · AI 前沿情报';
  content.innerHTML=`<section class="daily-lead"><div class="daily-inner">
    <div class="eyebrow">更新于 ${fmt(report.reportDate)} · 每天 5 分钟看完</div>
    <h1>最新 AI 情报</h1>
    <p>持续更新已经审核发布、值得开发者关注的 AI 动态。</p>
    <div class="daily-count">共 ${report.events.length} 篇已发布情报</div>
  </div></section>
  <div class="feed-shell">${Object.keys(sectionLabels).map(section=>{
    const items=report.events.filter(item=>item.section===section);
    return items.length?`<section class="feed-section" id="${sectionIds[section]}"><div class="section-heading"><span>${sectionLabels[section]}</span><small>${items.length} 条</small></div>${items.map(item=>eventCard(item.event)).join('')}</section>`:'';
  }).join('')}</div>`;
}

const inline=value=>{const source=String(value||''),pattern=/`[^`]+`|\*\*[^*]+\*\*/g;let html='',last=0,match;while((match=pattern.exec(source))){html+=esc(source.substring(last,match.index));const token=match[0];html+=token.startsWith('`')?`<code>${esc(token.substring(1,token.length-1))}</code>`:`<strong class="reader-emphasis">${esc(token.substring(2,token.length-2))}</strong>`;last=pattern.lastIndex}return html+esc(source.substring(last))};
const articleSection=(title,value,className='')=>value?`<section class="article-section ${className}"><h2>${title}</h2><p>${inline(value)}</p></section>`:'';
const parseProse=value=>{const lines=String(value||'').split('\n').map(line=>line.trim()),sections=[];let current={title:'',blocks:[]},list=[];const flushList=()=>{if(list.length){current.blocks.push({type:'list',items:list});list=[]}};const flushSection=()=>{flushList();if(current.title||current.blocks.length)sections.push(current);current={title:'',blocks:[]}};lines.forEach(line=>{if(!line){flushList();return}if(line.startsWith('## ')){flushSection();current.title=line.substring(3).trim();return}const listItem=line.match(/^[-*]\s+(.+)$/);if(listItem){list.push(listItem[1]);return}flushList();if(line.startsWith('> ')){current.blocks.push({type:'quote',value:line.substring(2)});return}current.blocks.push({type:'paragraph',value:line})});flushSection();return sections};
const proseContentLength=sections=>sections.reduce((total,section)=>total+section.blocks.reduce((sum,block)=>sum+(block.value||block.items?.join('')||'').length,0),0);
const usesSectionHeadings=sections=>{const contentLength=proseContentLength(sections);return sections.filter(section=>section.title).length>=2&&contentLength>=800};
const prose=(sections,showHeadings=usesSectionHeadings(sections))=>sections.map((section,index)=>`<section class="reader-section${showHeadings&&section.title?' has-heading':''}" id="reader-section-${index+1}">${showHeadings&&section.title?`<h2>${esc(section.title)}</h2>`:''}<div class="reader-section-copy">${section.blocks.map(block=>block.type==='list'?`<ul class="reader-list">${block.items.map(item=>`<li>${inline(item)}</li>`).join('')}</ul>`:block.type==='quote'?`<blockquote>${inline(block.value)}</blockquote>`:`<p>${inline(block.value)}</p>`).join('')}</div></section>`).join('');
const articleToc=sections=>{const links=sections.map((section,index)=>section.title?`<a href="#reader-section-${index+1}">${esc(section.title)}</a>`:'').filter(Boolean),contentLength=proseContentLength(sections);return links.length>=5&&contentLength>=1200?`<aside class="article-rail"><div class="article-rail-label">本文导读</div><nav aria-label="文章目录">${links.join('')}</nav></aside>`:''};
const bindArticleScroll=()=>{const header=document.querySelector('.article-header');if(!header)return;const update=()=>document.body.classList.toggle('is-reading',window.scrollY>header.offsetTop+header.offsetHeight-80);update();window.addEventListener('scroll',update,{passive:true})};

function developerSnapshot(event,product){
  const tagItems=tags(product?.capabilityTags);
  return `<section class="article-section"><h2>开发者速览</h2><div class="snapshot-grid">
    <div><span>可用状态</span><strong>${esc(availability(product?.availability||event.availability))}</strong></div>
    ${product?`<div><span>相关产品</span><strong><a href="/product/${esc(product.slug)}">${esc(product.name)}</a></strong></div>`:''}
    ${product?`<div><span>是否开源</span><strong>${product.openSource?'是':'否'}</strong></div>`:''}
  </div>
  ${tagItems.length?`<div class="tag-list">${tagItems.map(tag=>`<span>${esc(tag)}</span>`).join('')}</div>`:''}
  ${product?`<div class="resource-links">${externalLink(product.officialUrl,'官网')}${externalLink(product.docsUrl,'文档')}${externalLink(product.apiUrl,'API')}${externalLink(product.githubUrl,'GitHub')}</div>`:''}
  </section>`;
}

function productTimeline(product,currentId){
  if(!product)return '';
  const recent=(product.events||[]).slice(0,5);
  return `<section class="article-section product-moments"><div class="section-heading"><span>${esc(product.name)} 最近动态</span></div>
    <div class="timeline">${recent.map(item=>`<a class="timeline-item ${Number(item.eventId)===Number(currentId)?'current':''}" href="/event/${item.eventId}"><time>${fmt(item.eventTime)}</time><strong>${esc(item.title)}</strong>${Number(item.eventId)===Number(currentId)?'<em>当前文章</em>':''}</a>`).join('')||'<p class="muted">暂无其他动态</p>'}</div>
    <a class="profile-link" href="/product/${esc(product.slug)}">查看完整产品档案 →</a>
  </section>`;
}

function sources(event){
  return `<section class="article-section source-section"><h2>原始来源</h2>${(event.articles||[]).map(article=>`<article class="source-item">
    <div class="source-kind">${article.primarySource?'官方来源':'相关报道'}</div>
    <h3><a target="_blank" rel="noopener" href="${esc(article.url)}">${esc(article.title)}</a></h3>
    <p>${esc(article.sourceName)} · ${fmt(article.publishedAt)}</p>
  </article>`).join('')||'<p class="muted">来源信息暂未公开。</p>'}</section>`;
}

async function eventPage(){
  const id=location.pathname.split('/').pop();
  const [event,navigation]=await Promise.all([get('/api/public/events/'+id),get('/api/public/events/'+id+'/navigation')]);
  const primaryProduct=event.products?.[0]?await get('/api/public/products/'+encodeURIComponent(event.products[0].slug)).catch(()=>null):null;
  const reader=event.readerArticle;
  const readerSections=reader?parseProse(reader.readerContent):[];
  const readerUsesHeadings=usesSectionHeadings(readerSections);
  const toc=readerUsesHeadings?articleToc(readerSections):'';
  const title=reader?.readerTitle||event.title,lead=reader?.readerLead||event.summary;
  document.title=`${title} · AI 前沿情报`;
  const productText=products(event);
  content.innerHTML=`<article class="article-shell">
    <div class="article-return"><a class="back-link" href="/">← 返回今日日报</a></div>
    <header class="article-header"><div class="article-meta"><span class="article-category">${sectionLabels[event.category]||'AI 情报'}</span><span>${fmt(event.publishedAt||event.eventTime)}</span><span>${readingTime(reader?{summary:reader.readerLead,whatHappened:reader.readerContent,whyImportant:reader.readerConclusion}:event)}</span>${productText?`<span>${productText}</span>`:''}</div><h1>${esc(title)}</h1><p class="dek">${inline(lead||'')}</p></header>
    <div class="article-body${toc?' has-rail':''}">
      ${toc}
      <div class="article-main">
        ${reader?`<div class="article-section article-opening reader-content${readerUsesHeadings?'':' is-continuous'}">${prose(readerSections,readerUsesHeadings)}</div>`:`${articleSection(`${esc(primaryProduct?.name||'这次')} 更新了什么？`,event.whatHappened,'article-opening')}${articleSection('为什么值得关注',event.whyImportant)}${articleSection('现在能不能用',event.availability)}${articleSection('对开发者意味着什么',event.developerImpact)}`}
        ${developerSnapshot(event,primaryProduct)}
        ${articleSection('现在是否值得关注',reader?.readerConclusion||event.whyImportant,'article-conclusion')}
        ${productTimeline(primaryProduct,id)}
        ${sources(event)}
        <nav class="article-navigation">${navigation.previous?`<a href="/event/${navigation.previous.id}"><span>上一篇</span><strong>${esc(navigation.previous.title)}</strong></a>`:'<span></span>'}${navigation.next?`<a class="next" href="/event/${navigation.next.id}"><span>下一篇</span><strong>${esc(navigation.next.title)}</strong></a>`:''}</nav>
        <a class="back-report" href="/">← 返回今日日报</a>
      </div>
    </div>
  </article>`;
  bindArticleScroll();
}

async function productPage(){
  const slug=decodeURIComponent(location.pathname.split('/').pop());
  const product=await get('/api/public/products/'+encodeURIComponent(slug));
  document.title=`${product.name} · AI 前沿情报`;
  const tagItems=tags(product.capabilityTags);
  content.innerHTML=`<article class="product-shell">
    <nav class="breadcrumb article-toolbar" aria-label="页面位置"><a class="back-link" href="/">← 返回首页</a><span aria-hidden="true">/</span><strong>产品档案</strong></nav>
    <header class="product-hero"><div class="eyebrow">AI 产品档案</div><h1>${esc(product.name)}</h1><p>${esc(product.summary||'')}</p><div class="product-facts"><span>由 ${esc(product.companyName||'开发团队')} 开发</span><span>${esc(availability(product.availability))}</span>${product.openSource?'<span>开源项目</span>':''}</div><div class="resource-links">${externalLink(product.officialUrl,'官网')}${externalLink(product.githubUrl,'GitHub')}${externalLink(product.docsUrl,'文档')}${externalLink(product.apiUrl,'API')}</div></header>
    ${tagItems.length?`<section class="product-capabilities"><h2>它能做什么？</h2><div class="tag-list">${tagItems.map(tag=>`<span>${esc(tag)}</span>`).join('')}</div></section>`:''}
    <section class="product-history"><div class="section-heading"><span>最近发生了什么？</span><small>${(product.events||[]).length} 条动态</small></div><div class="timeline">${(product.events||[]).map(item=>`<a class="timeline-item" href="/event/${item.eventId}"><time>${fmt(item.eventTime)}</time><strong>${esc(item.title)}</strong><p>${esc(item.summary||'')}</p></a>`).join('')||'<div class="empty">暂无已发布动态</div>'}</div></section>
  </article>`;
}

async function searchPage(){
  const form=document.querySelector('#search');
  const run=async()=>{
    const query=new URLSearchParams(location.search).get('q')||'';
    form.q.value=query;
    if(!query){content.innerHTML='<p class="search-hint">输入产品名、公司或关键词，查找已经发布的 AI 情报。</p>';return}
    const result=await get('/api/public/search?q='+encodeURIComponent(query));
    content.innerHTML=`<section class="search-results"><h2>相关文章</h2>${result.events.map(eventCard).join('')||'<div class="empty">没有找到相关文章</div>'}<h2>相关产品</h2>${result.products.map(product=>`<article class="story-card"><h3><a href="/product/${esc(product.slug)}">${esc(product.name)}</a></h3><p>${esc(product.summary||'')}</p></article>`).join('')||'<div class="empty">没有找到相关产品</div>'}</section>`;
  };
  form.onsubmit=event=>{event.preventDefault();location.search='?q='+encodeURIComponent(form.q.value)};
  run();
}

(async()=>{try{if(window.PAGE==='event')await eventPage();else if(window.PAGE==='product')await productPage();else if(window.PAGE==='search')await searchPage();else await home()}catch(error){content.innerHTML=`<div class="empty">${esc(error.message)}</div>`}})();
