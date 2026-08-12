import{x as g,o as w,l as z,h as M,c as v,r as b}from"./index-Dmb_Vifr.js";const F={__name:"SnowEffect",props:{intensity:{type:String,default:"light",validator:p=>["light","medium","heavy"].includes(p)}},setup(p){const u=p,l=b(null);let i=null,r=[];const f={light:{count:45,minSize:4,maxSize:8,minSpeed:.3,maxSpeed:.8,opacity:.5,swayRange:40},medium:{count:75,minSize:5,maxSize:10,minSpeed:.5,maxSpeed:1,opacity:.6,swayRange:60},heavy:{count:120,minSize:6,maxSize:12,minSpeed:.7,maxSpeed:1.4,opacity:.7,swayRange:80}};function S(n,o,s){const a=n.minSize+Math.random()*(n.maxSize-n.minSize),t=n.opacity*(.5+Math.random()*.5),h=n.minSpeed+Math.random()*(n.maxSpeed-n.minSpeed),c=(20+Math.random()*n.swayRange)*(Math.random()>.5?1:-1),e=.5+Math.random()*1.5,m=Math.random()*Math.PI*2,x=document.createElement("div");return x.style.cssText=`
    position: absolute;
    left: 0;
    top: 0;
    width: ${a}px;
    height: ${a}px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255,255,255,${t}) 0%, rgba(210,225,250,${t*.5}) 100%);
    box-shadow: 0 0 ${a*.8}px rgba(255,255,255,${t*.4});
    will-change: transform;
    pointer-events: none;
  `,{el:x,x:Math.random()*s,y:-a-Math.random()*o,speed:h,swayAmplitude:c,swaySpeed:e,phase:m,size:a}}function y(){if(!l.value)return;const n=l.value;n.innerHTML="",r=[];const o=n.getBoundingClientRect(),s=f[u.intensity];for(let a=0;a<s.count;a++){const t=S(s,o.height,o.width);n.appendChild(t.el),r.push(t)}d(0)}function d(n){const o=l.value;if(!o)return;const s=o.getBoundingClientRect(),a=s.height,t=s.width,h=n/1e3;for(let c=0;c<r.length;c++){const e=r[c];e.y+=e.speed;const m=Math.sin(h*e.swaySpeed+e.phase)*e.swayAmplitude;e.y>a+e.size&&(e.y=-e.size,e.x=Math.random()*t),e.x+m>t+e.size?e.x-=t+e.size:e.x+m<-e.size&&(e.x+=t+e.size),e.el.style.transform=`translate(${e.x+m}px, ${e.y}px)`}i=requestAnimationFrame(d)}return g(()=>u.intensity,()=>{i&&cancelAnimationFrame(i),y(),i=requestAnimationFrame(d)}),w(()=>{y(),i=requestAnimationFrame(d)}),z(()=>{i&&(cancelAnimationFrame(i),i=null),r=[]}),(n,o)=>(M(),v("div",{ref_key:"containerRef",ref:l,class:"absolute inset-0 pointer-events-none overflow-hidden"},null,512))}};export{F as _};
