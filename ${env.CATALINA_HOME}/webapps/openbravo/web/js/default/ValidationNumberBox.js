validateNumberBox=function(b){isValidNumberBox(b);var a=document.getElementById(b).getAttribute("required");if(a=="true"){isMissingNumberBox(b);}};isValidNumberBox=function(c){var b=isNumber(document.getElementById(c).value,true,true);var a=document.getElementById(c+"invalidSpan");if(b){a.style.display="none";
}else{a.style.display="";}};isMissingNumberBox=function(c){var b=document.getElementById(c).value.length==0;var a=document.getElementById(c+"missingSpan");if(b){a.style.display="";}else{a.style.display="none";}};isNumber=function(g,a,e){var d=false;var f=false;var b=0;if(g==null||g==""){return true;}if(g.substring(b,b+1)=="-"){if(e!=null&&e){f=true;
b++;}else{return false;}}else{if(g.substring(b,b+1)=="+"){b++;}}var c=g.length;for(b=b;b<c;b++){if(isNaN(g.substring(b,b+1))){if(a&&g.substring(b,b+1)=="."&&!d){d=true;}else{return false;}}}return true;};