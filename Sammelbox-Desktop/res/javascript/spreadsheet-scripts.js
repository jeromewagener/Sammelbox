/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2014 Jerome Wagener, Paul Bicheler & Olivier Wagener
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

var minColWidth = 140;
var minimizedColWidth = 25;

/* global variables that are needed for the dragging functionality. */
var mouseDownX;
var idDragStarted;
var dragging = false;

/* global variables to store the item updates/additions/deletions 
 *  deleteTheseObjects is an array of IDs
 *  updateTheseObjects is an array of arrays. One inner array holds one entire row.
 *  elements in the updateTheseObjects array with an id smaller or equal to -1 are new elements. */
var updateTheseObjects = [];
var deleteTheseObjects = [];

var move = 0;
var tableHeight = 0;
var tableTop = 0;

/*  function that stores the x coordinate of the mouse during an onMouseDown event on the resize area.
 *  furthermore, it disables the ability to select text inside the <body></body> tags which would leed
 *  to unexpected sizes during the onMouseUp event. */
function startDrag(id, event) {
	idDragStarted = id;
	mouseDownX = event.clientX;
	dragging = true;
	
	tableHeight = document.getElementById('spreadsheetTable').offsetHeight;
	tableTop = document.getElementById('spreadsheetTable').offsetTop;
	
	var body = document.getElementById('body');
	if (!hasClass(body, 'unselectable')) {
		addClass(body, 'unselectable');
	}
	
	var dragPreview = document.getElementById('dragPreview');
	if (hasClass(dragPreview, 'hidden')) {
		removeClass(dragPreview, 'hidden');
	}
}	
	
function moveDiv(e) {
	move++;	

	if (move % 5 == 0) {
    	document.getElementById("dragPreview").style.top = tableTop + "px";
    	document.getElementById("dragPreview").style.height = tableHeight + "px";
  		document.getElementById("dragPreview").style.left = (e.pageX + 20) + "px";
  	}
}

/*  function that calculates the delta between the onMouseDown x and onMouseUp x coordinates. 
 *  if this delta is smaller than the minColWidth constant, the delta is set to minColWidth.
 *  it also reenables the functioonality to select text inside the <body></body> tags. */
function stopDrag(event) {
	if (dragging == false) {
		return;
	}
	
	mouseUpX = event.clientX;
	deltaX = mouseUpX - mouseDownX;	
	
	oldWidth = parseInt(document.getElementById('col:' + idDragStarted).getAttribute('width'));	
	newWidth = oldWidth + deltaX;
	if (newWidth < minColWidth) {
		newWidth = minColWidth;	
	}
	
	document.getElementById('col:' + idDragStarted).setAttribute('width', newWidth);			
	
	dragging = false;
	
	var body = document.getElementById('body');
	if (hasClass(body, 'unselectable')) {
		removeClass(body, 'unselectable');
	}
	
	var dragPreview = document.getElementById('dragPreview');
	if (!hasClass(dragPreview, 'hidden')) {
		addClass(dragPreview, 'hidden');
	}
}

/*  function to invert the hidden class attribute of an element. */
function invertHidden(elem) {
	if (hasClass(elem, 'hidden')) {
		removeClass(elem, 'hidden');
	} else {
		addClass(elem, 'hidden');
	}
}

/*  function to invert the hidden class attribute of an entire column in the spreadsheet. 
 *  she also toogles the visability of the column controle elements and the width of the
 *  column from minColWidth to minimizedColWidth and vice versa. */
function setHidden(column) {
	var elem = document.getElementById('label:' + column);		
	invertHidden(elem);
	
	elem = document.getElementById('dragMe:' + column);				
	invertHidden(elem);
	
	elem = document.getElementById('arrowRight:' + column);				
	invertHidden(elem);
	
	elem = document.getElementById('arrowLeft:' + column);				
	invertHidden(elem);
	
	for(var index = 0; index < tableRowId.length; index++) {
		elem = document.getElementById('hideThisContainer:' + column + ":" + tableRowId[index]);	
		invertHidden(elem);
	}
	
	elem = document.getElementById('col:' + column);	
	
	if (parseInt(elem.getAttribute('width')) != minimizedColWidth) {
		elem.setAttribute('width', minimizedColWidth);			
	} else {
		elem.setAttribute('width', minColWidth);	
	}
}

/*  function to mark a row as a row the user wants to delete.
 *  adds to the row (<tr>) the class attribute 'delete' and adds the
 *  id of record to an array that is send during the updateAndDelete. */
function markAsDelete(id) {
	var checkBox = document.getElementById('delete:' + id);
	var row = document.getElementById('row:' + id);
	var checkState = checkBox.checked;
	
	if (checkState == true) {
		addClass(row, 'delete');
		increaseDeleteCount();
	} else {
		removeClass(row, 'delete');
		decreaseDeleteCount();
	}
	
	if (contains(deleteTheseObjects, id)) {
		remove(deleteTheseObjects, id);	
	} else {
		deleteTheseObjects.push(id);
	}	
}

/*  function to add the class atribute 'wrongInput' to an object.
 *  usually this object is a cell (<td>) which contains one input field. */
function fieldIsWrong(cell, id) {	
	if (!hasClass(cell, 'wrongInput')) {
		addClass(cell, 'wrongInput');
		incWrongCounter(id);
	}
}

/*  function to remove the class atribute 'wrongInput' from an object.
 *  usually this object is a cell (<td>) which contains one input field. */
function fieldIsOk(cell, id) {
	if (hasClass(cell, 'wrongInput')) {
		removeClass(cell, 'wrongInput');
		decWrongCounter(id);
	}
}

/*  function that handels the edition of rows. It is in charge of adding a class
 *  attribute to the row (<tr>) as well as checking if the ID is the ID of the 
 *  empty spare row. In that case, it calls the function to handel the addition of
 *  fields. For cells with a particuar type (such as INTEGER), a check with a regex is
 *  executed. Also, for every modified row, the entire row is pushed to an array
 *  which is send to the updateAndDelete java method. */
function markAsDirty(id, columnIndex) {	
	var row = document.getElementById('row:' + id);	
	var nextFreeId = document.getElementById('nextFreeId').innerHTML;
	var field = document.getElementById('input:' + columnIndex + ":" + id);
	var cell = document.getElementById('value:' + columnIndex + ":" + id); 	
	
	var colType = tableColType[posInArray(tableColId, columnIndex)];
			
   if (spreadsheetTypeValidatorFunction(colType, field.value)) {
		fieldIsOk(cell, id);
	} else {
		fieldIsWrong(cell, id);
	}
	
	if (id == nextFreeId) {
		newItem(id);
	} else {	
		if (!hasClass(row, 'dirty')) {
			addClass(row, 'dirty');
			if(id > 0) {
				increaseModifyCount();	
			}
		}
	}
	
	for(var index = 0; index < updateTheseObjects.length; index++) {
		if (updateTheseObjects[index][0] == id) {
			remove(updateTheseObjects, updateTheseObjects[index]);	
		}
	}
	
	var changeObject = [];
	changeObject.push(id);
	
	/* Start from 1 because tableColId[0] = id which has no field in the spreadsheet */
	for(index = 1; index < tableColId.length; index++) {
		elem = document.getElementById('input:' + tableColId[index] + ":" + id);
		changeObject.push(elem.value);
	}
	
	updateTheseObjects.push(changeObject);
}

/*  In case of a wrong regular expression, the following two functions are used to increment 
 *  and decrement the row counters for wrong input fields. Then a function is called that sums
 *  these violations up. */
function decWrongCounter(id) {
	var counter = parseInt(document.getElementById("corruptions:" + id).value);
	counter = counter - 1;	
	document.getElementById("corruptions:" + id).value = counter;
	enableDisableSendButoon();
}

function incWrongCounter(id) {
	var counter = parseInt(document.getElementById("corruptions:" + id).value);
	counter = counter + 1;	
	document.getElementById("corruptions:" + id).value = counter;
	enableDisableSendButoon();
}

/*  This function sums the violations of the regular expression checked fields of the entire
 *  table up. Only if this number is equal to 0, the button to send the changes to the java
 *  programm is enabled. */
function enableDisableSendButoon() {
	var total = 0;	
	
	for(var index = 0; index < tableRowId.length; index++) {
		total = total + parseInt(document.getElementById('corruptions:' + tableRowId[index]).value);	
	}
	
	if (total == 0) {
		document.getElementById("checkAndSend").disabled = false;
	} else {
		document.getElementById("checkAndSend").disabled = true;	
	}	
}

/* A function to check if an array contains an object.*/
function contains(array, obj) {
	for (var i = 0; i < array.length; i++) {
		if (array[i] === obj) {
			return true;
		}
 	}
	return false;
}

/* A function to remove an object from an array without leaving an empty space.
 * array[1, 2, 3] => array[1, 3]
 */
function remove(arr, item) {
	for(var i = arr.length; i--;) {
		if(arr[i] == item) {
			arr.splice(i, 1);
		}
	}
}

/* A function to remove an array from an array without leaving an empty space.
 * This function assumes that the inner array has the key to search for at its position [0]
 * array[[1, 'some'], [2, 'random'], [3, 'text']] => array[[1, 'some'], [3, 'text']]
 */
function removeByKey(arr, key) {
	for(var i = arr.length; i--;) {
		if(arr[i][0] == key) {
			arr.splice(i, 1);
		}
	}
}

/* A function to return the position of an item in an array.*/
function posInArray(arr, item) {
	for (var i = 0; i < arr.length; i++) {
		if (arr[i] == item) {
			return i;
		}
 	}
 	return -1;	
}

/*  A function that is called by markAsDirty() function. It removes the 'empty' class attribute
 *  and adds the 'new' class attribute. Then, the entire row is packed into an array which is 
 *  added to the updateTheseObjects array.
 */
function newItem(id) {
	var row = document.getElementById('row:' + id);	
	
	if (hasClass(row, 'empty')) {
		cloneRow(id);
		removeClass(row, 'empty');
		
		if ((id % 2) == 0) {
			addClass(row, 'newEven');
		}	else {
			addClass(row, 'newOdd');
		}
	}
	
	var changeObject = [];
	changeObject.push(id);
	
	for(var index = 1; index < tableColId.length; index++) {
		elem = document.getElementById('input:' + tableColId[index] + ":" + id);
		changeObject.push(elem.value);
	}
	
	updateTheseObjects.push(changeObject);
}

/*  Function that is triggered during the modification on the spare(empty) cell.
 *  It creates a new spare row and prepares her already with a new ID. It removes all
 *  the 'wrongInput' class attributes that can occur when a row with violations is 
 *  copied.
 */ 
function cloneRow(id) {
	var row = document.getElementById("row:" + id);
	var table = document.getElementById("spreadsheetTable");
	var clone = row.cloneNode(true);

	newId = id - 1;	
	clone.id = 'row:' + (id - 1);

	tableRowId.push(id - 1);
	
	document.getElementById('nextFreeId').innerHTML = id - 1;

	str = clone.innerHTML;
	clone.innerHTML = str.replaceAll(id, newId, true);
	
	table.appendChild(clone);
	increaseRowCount();
	
	/* Start from 1 because tableColId[0] = id which has no field in the spreadsheet */
	for(index = 1; index < tableColId.length; index++) {
		elem = document.getElementById('value:' + tableColId[index] + ":" + newId);
		if (hasClass(elem, 'wrongInput')) {
			removeClass(elem, 'wrongInput');
		}
	}	
	
	var deleteDiv	= document.getElementById('deletediv:' + id);
	var deleteTd	= document.getElementById('delete:' + id);
	
	if (hasClass(deleteDiv, 'hidden')) {
		removeClass(deleteDiv, 'hidden');
	}
		
	if (hasClass(deleteTd, 'whiteBorderless')) {
		removeClass(deleteTd, 'whiteBorderless');
	}
}

function deleteRow(id) {
	var rowToDelete = document.getElementById("row:" + id);
	var table = document.getElementById("spreadsheetTable");
	
	var rowCount = table.rows.length;
  
	for (var i = 0; i < rowCount; i++) {
		var row = table.rows[i];

		if (row == rowToDelete) {
			table.deleteRow(i);
			removeByKey(updateTheseObjects, id);
			decreaseRowCount();
			return;
		}
	}
}

/* Functions to increase/decrease the counters that are shown on the bottom of the Spreadsheet. */
function increaseRowCount() {
	document.getElementById('addCount').innerHTML = parseInt(document.getElementById('addCount').innerHTML) + 1
}

function decreaseRowCount() {
	document.getElementById('addCount').innerHTML = parseInt(document.getElementById('addCount').innerHTML) - 1
}

function increaseDeleteCount() {
	document.getElementById('deleteCount').innerHTML = parseInt(document.getElementById('deleteCount').innerHTML) + 1
}

function decreaseDeleteCount() {
	document.getElementById('deleteCount').innerHTML = parseInt(document.getElementById('deleteCount').innerHTML) - 1
}

function increaseModifyCount() {
	document.getElementById('modifyCount').innerHTML = parseInt(document.getElementById('modifyCount').innerHTML) + 1
}

function checkAndSend() {	
	if (updateConfirmationDialogFunction()) {
		spreadsheetUpdateFunction(tableColName, tableColType, updateTheseObjects, deleteTheseObjects);
	}
}

/* The functions for adding, removing and checking of classes have been copied from this source.
 * http://blog.dirk-helbert.de/blog/2010/08/12/mit-javascript-css-klassen-hinzufugen-und-entfernen/ 
 */
function addClass(ele, cls) {
    if (!this.hasClass(ele, cls)) {
    	ele.className += " " + cls;
    }
}

function removeClass(ele, cls) {
    if (hasClass(ele, cls)) {
        var reg = new RegExp('(\\s|^)'+cls+'(\\s|$)');
        ele.className=ele.className.replace(reg,' ');
    }
}

function hasClass(ele, cls) {
    return ele.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'));
}

/* ReplaceAll by Fagner Brack (MIT Licensed)
 * Replaces all occurrences of a substring in a string
 */
String.prototype.replaceAll = function(token, newToken, ignoreCase) {
    var _token;
    var str = this + "";
    var i = -1;

    if ( typeof token === "string" ) {

        if ( ignoreCase ) {

            _token = token.toLowerCase();

            while( (
                i = str.toLowerCase().indexOf(
                    token, i >= 0 ? i + newToken.length : 0
                ) ) !== -1
            ) {
                str = str.substring( 0, i ) +
                    newToken +
                    str.substring( i + token.length );
            }

        } else {
            return this.split( token ).join( newToken );
        }

    }
    
    return str;
};
