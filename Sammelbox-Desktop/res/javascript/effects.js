	
	/*** Picture Swap ***/

	function change(id, imgName) {
		document.getElementById(id).src=imgName; 
	}

	function showBigPicture(id) {  
		var str0="show:///bigPicture="; 
		var str1=document.getElementById(id).src; 
		var str2="?"; 
		var str3=id; 

		parent.location.href=((str0.concat(str1)).concat(str2)).concat(str3);  
	} 



	/*** Mouse Cursor Modification ***/

	function changeCursorToHand(id) {
		document.getElementById(id).style.cursor="pointer"; 
	}



	/*** Scrollbar Position Calculation ***/

	function getWindowHeight() {
	    return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight || 0;
	}

	function getYScrollPosition() {
	    return window.pageYOffset || document.body.scrollTop || document.documentElement.scrollTop || 0;
	}

	function getDocumentHeight() {
	    return Math.max(document.body.scrollHeight || 0, document.documentElement.scrollHeight || 0,
		            document.body.offsetHeight || 0, document.documentElement.offsetHeight || 0,
		            document.body.clientHeight || 0, document.documentElement.clientHeight || 0);
	}

	function getScrollbarPercentage() {
	    return ((getYScrollPosition() + getWindowHeight()) / getDocumentHeight()) * 100;
	}

	function getScrollPixels() {
		return getDocumentHeight() - (getYScrollPosition() + getWindowHeight())
	}



	/*** Dynamic Table Modification ***/

	function triggerAlbumExpansionIfNecessary() {
		if (getScrollPixels() < 200) {	
			parent.location.href="show:///addAdditionalAlbumItems";
		}
	}

	window.onscroll = function() { 
		triggerAlbumExpansionIfNecessary();
	}

   /** Add resize listener */

   window.onresize = function() {
      window.location.href="show:///browserResized";
   }

   /*** Maximizes the given image to the window size. However, if the image is smaller
      than the window, the image will not be stretched! ***/

   function maximizeImageSize(imageId, xOffset, yOffset) {     
      document.getElementById(imageId).setAttribute("style",'width:auto');
      document.getElementById(imageId).setAttribute("style",'height:auto');
      
      if ((window.innerHeight < document.getElementById(imageId).naturalHeight)
         || (window.innerWidth < document.getElementById(imageId).naturalWidth))
      {
         if (document.getElementById(imageId).naturalWidth > document.getElementById(imageId).naturalHeight) {    
         	newWidth = window.innerWidth - xOffset;        
             document.getElementById(imageId).setAttribute("style",'width:' + newWidth + 'px');
         } else {
         	newHeight = window.innerHeight - yOffset;
            document.getElementById(imageId).setAttribute("style",'height:' + newHeight + 'px');
         }
      }
   }





