function dragPan(ideogram) {
	var el = ideogram.body.dom;
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
//     el.scrollTop += (y0 - y);
//     el.scrollLeft += (x0 - x);
     var deltaX = x - x0;
     var deltaY = y - y0;

     // No need to look to the left of chromosome 1
     if (ideogram.currentTransform.x + deltaX > 0) {
    	 deltaX = -1 * ideogram.currentTransform.x;
     }
     
     // No need to look above the chromosome labels
     if (ideogram.currentTransform.y + deltaY > 0) {
    	 deltaY = -1 * ideogram.currentTransform.y;
     }
     
     // No need to look to the right of chromosome Y
     if (ideogram.currentTransform.x + deltaX < ideogram.boxWidth - ideogram.width) {
    	 deltaX = ideogram.boxWidth - ideogram.width - ideogram.currentTransform.x;
     }
     
     // No need to look below the longest chromosome
     if (ideogram.currentTransform.y + deltaY < ideogram.boxHeight - ideogram.height) {
    	 deltaY = ideogram.boxHeight - ideogram.height - ideogram.currentTransform.y;
     }
     
     if (deltaX != 0 || deltaY != 0 ) {
    	 ideogram.ctx.translate(deltaX, deltaY);
    	 ideogram.ctxOverlay.translate(deltaX, deltaY);
    	 ideogram.ctxSelection.translate(deltaX, deltaY);
    	 ideogram.currentTransform.x += deltaX;
    	 ideogram.currentTransform.y += deltaY;
    	 ideogram.redraw();
     }
     
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