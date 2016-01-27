function dragPan(el) {
 function startPan(event) {
   
   if (event.button != 2) {
     return;
   }
   var originalCursor = el.style.cursor;
   el.style.cursor = 'move';
   var x0 = event.screenX,
       y0 = event.screenY;
   function continuePan(event) {
     var x = event.screenX,
         y = event.screenY;
     el.scrollTop += (y0 - y);
     el.scrollLeft += (x0 - x);
     x0 = x;
     y0 = y;
   }
   function stopPan(event) {
     window.removeEventListener('mousemove', continuePan);
     window.removeEventListener('mouseup', stopPan);
     el.style.cursor = originalCursor;
   };
   window.addEventListener('mousemove', continuePan);
   window.addEventListener('mouseup', stopPan);
   window.addEventListener('contextmenu', cancelMenu);
 };
 function cancelMenu(event) {
   event.preventDefault() 
   window.removeEventListener('contextmenu', cancelMenu);
   return false;
 }
 el.addEventListener('mousedown', startPan);
}