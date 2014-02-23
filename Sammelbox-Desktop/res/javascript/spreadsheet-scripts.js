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

var mouseDownX;
var newMouseX;
var idDragStarted;
var dragging = false;
var minColWidth = 170;

function startDrag(id, event) {
	idDragStarted = id;
	mouseDownX = event.clientX;
	dragging = true;
	
	var body = document.getElementById('body');
	if (!hasClass(body, 'unselectable')) {
		addClass(body, 'unselectable');
	}
}

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
}

function invertHidden(elem) {
	if (hasClass(elem, 'hidden')) {
		removeClass(elem, 'hidden');
	} else {
		addClass(elem, 'hidden');
	}
}

function setHidden(column) {
	var rowCount = document.getElementById('rowCount').innerHTML;		
	
	var elem = document.getElementById('label:' + column);		
	invertHidden(elem);
	
	elem = document.getElementById('dragMe:' + column);				
	invertHidden(elem);
	
	elem = document.getElementById('arrowRight:' + column);				
	invertHidden(elem);
	
	elem = document.getElementById('arrowLeft:' + column);				
	invertHidden(elem);
	
	for(index = 0; index < tableRowId.length; ++index) {
		elem = document.getElementById('hideThisContainer:' + column + ":" + tableRowId[index]);	
		invertHidden(elem);
	}
	
	elem = document.getElementById('col:' + column);	
	
	if (parseInt(elem.getAttribute('width')) != 25) {
		elem.setAttribute('width', 25);			
	} else {
		elem.setAttribute('width', 170);	
	}
}

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
}

function markAsDirty(id) {	
	var row = document.getElementById('row:' + id);	
	
	if (id < 0) {
		newItem(id);
	} else {	
		if (!hasClass(row, 'dirty')) {
			addClass(row, 'dirty');
			increaseModifyCount();
		}
	}
}

function newItem(id) {
	var row = document.getElementById('row:' + id);	
	
	if (hasClass(row, 'empty')) {
		cloneRow(id);
		removeClass(row, 'empty');
		addClass(row, 'new');
	}
}
 
function cloneRow(id) {
	var row = document.getElementById("row:" + id);
	var table = document.getElementById("spreadsheetTable");
	var clone = row.cloneNode(true);

	newId = id - 1;	
	clone.id = 'row:' + (id - 1);

	tableRowId.push(id - 1);

	str = clone.innerHTML;
	clone.innerHTML = str.replaceAll(id, newId, true);
	
	table.appendChild(clone);
	increaseRowCount();
}

function increaseRowCount() {
	document.getElementById('addCount').innerHTML = parseInt(document.getElementById('addCount').innerHTML) + 1
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