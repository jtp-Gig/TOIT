function onResizeDo(){var e=document.getElementById("LinksCont");var c=document.getElementById("TextCont");var q=document.getElementById("LogoCont");var d=document.getElementById("LogoMiddleCont");var g=document.getElementById("LogoLeftCont");var m=document.getElementById("LogoRightCont");var j=document.getElementById("TextBody");
var h=document.getElementById("TextScrollCont");var f=document.getElementById("TextScrollUp");var o=document.getElementById("TextScrollMiddle");var p=document.getElementById("TextScrollDown");var i=document.getElementsByTagName("BODY");var n=i[0].clientHeight;var t=i[0].clientWidth;e.style.display="";
c.style.display="";q.style.display="";c.style.height="";var u=t-e.clientWidth-30;if(u<0){u=0;}c.style.width=u;c.style.height=146;j.style.height=102;var r=parseInt(u/2,10)-2;var l=1;var k=u-l-r-2;if(r<0){r=0;}if(k<0){k=0;}f.style.width=r;o.style.width=l;p.style.width=k;if(j.scrollTop===0){document.getElementById("TextScrollUp").className="TextScrollUp TextScrollUp_disabled";
}else{document.getElementById("TextScrollUp").className="TextScrollUp TextScrollUp_normal";}if(j.scrollTop>=j.scrollHeight-j.clientHeight){document.getElementById("TextScrollDown").className="TextScrollDown TextScrollDown_disabled";}else{document.getElementById("TextScrollDown").className="TextScrollDown TextScrollDown_normal";
}if(j.scrollHeight>j.clientHeight+6){j.style.height=87;h.style.display="";}else{j.scrollTop=0;h.style.display="none";j.style.height=102;}if(j.scrollTop>=j.scrollHeight-j.clientHeight){j.scrollTop=j.scrollHeight+100;}if(j.scrollTop>=j.scrollHeight-j.clientHeight){document.getElementById("TextScrollDown").className="TextScrollDown TextScrollDown_disabled";
}else{document.getElementById("TextScrollDown").className="TextScrollDown TextScrollDown_normal";}var b=e.clientWidth+c.clientWidth+10;if(b<0){b=0;}q.style.width=b;var a=q.clientWidth-g.clientWidth-m.clientWidth-23;if(a<0){a=0;}d.style.width=a;var s=(document.getElementById("LogoMiddleText").innerHTML.length*13)+20;
if(d.clientWidth>s){document.getElementById("LogoMiddleText").style.display="";}else{document.getElementById("LogoMiddleText").style.display="none";}}var step=1;var timerDown;var timerUp;function scrollButton(b,d){var e=b.id.replace("TextScroll","");var a=document.getElementById("TextBody");var c="normal";
if(d==="onmousedown"){if(e==="Up"){a.scrollTop-=step;timerUp=setTimeout(function(){scrollButton(b,d);},10);}else{if(e==="Down"){a.scrollTop+=step;timerDown=setTimeout(function(){scrollButton(b,d);},10);}}c="active";}else{if(d==="onmouseup"){clearTimeout(timerUp);clearTimeout(timerDown);c="hover";}else{if(d==="onmouseout"){clearTimeout(timerUp);
clearTimeout(timerDown);c="normal";}else{if(d==="onmouseover"){c="hover";}}}}if(a.scrollTop===0){document.getElementById("TextScrollUp").className="TextScrollUp TextScrollUp_disabled";if(e==="Up"){c="disabled";}}else{if(e==="Down"){document.getElementById("TextScrollUp").className="TextScrollUp TextScrollUp_normal";
}}if(a.scrollTop>=a.scrollHeight-a.clientHeight){document.getElementById("TextScrollDown").className="TextScrollDown TextScrollDown_disabled";if(e==="Down"){c="disabled";}}else{if(e==="Up"){document.getElementById("TextScrollDown").className="TextScrollDown TextScrollDown_normal";}}b.className="TextScroll"+e+" TextScroll"+e+"_"+c;
}