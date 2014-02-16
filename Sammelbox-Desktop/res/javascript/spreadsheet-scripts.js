function markAsDelete(id) {
	
	var checkBox = document.getElementById('delete:' + id);
	var row = document.getElementById('row:' + id);
	var checkState = checkBox.checked;
	
	if(checkState == true) {
		addClass(row, 'delete');
		increaseDeleteCount();
	}
	else {
		removeClass(row, 'delete');
		decreaseDeleteCount();
	}
}

function markAsDirty(id) {	
	
	var row = document.getElementById('row:' + id);	
	
	if(id < 0) {
		newItem(id);
	}else {	
		if (!hasClass(row, 'dirty')) {
			addClass(row, 'dirty');
			increaseModifyCount();
		}
	}
}

function newItem(id)
{

	var row = document.getElementById('row:' + id);	
	
	if (hasClass(row, 'empty')) {
		cloneRow(id);
		removeClass(row, 'empty');
		addClass(row, 'new');
	}
}
 
function cloneRow(id){
	var row = document.getElementById("row:"+id); // find row to copy
	var table = document.getElementById("spreadsheetTable"); // find table to append to
	var clone = row.cloneNode(true); // copy children too

	newId = id - 1;	
	clone.id = 'row:' + (id -1); // change id or other attributes/contents

	str = clone.innerHTML;
	clone.innerHTML = str.replaceAll(id, newId, true);
	
	table.appendChild(clone); // add new row to end of table
	increaseRowCount();
}

function increaseRowCount(){
	document.getElementById('addCount').innerHTML = parseInt(document.getElementById('addCount').innerHTML) + 1
}

function increaseDeleteCount(){
	document.getElementById('deleteCount').innerHTML = parseInt(document.getElementById('deleteCount').innerHTML) + 1
}

function decreaseDeleteCount(){
	document.getElementById('deleteCount').innerHTML = parseInt(document.getElementById('deleteCount').innerHTML) - 1
}

function increaseModifyCount(){
	document.getElementById('modifyCount').innerHTML = parseInt(document.getElementById('modifyCount').innerHTML) + 1
}


/*
 * The functions for adding, removing and checking of classes have been copied from this source.
 *
 * http://blog.dirk-helbert.de/blog/2010/08/12/mit-javascript-css-klassen-hinzufugen-und-entfernen/
 *
*/
function addClass(ele,cls) {
    if (!this.hasClass(ele,cls)) ele.className += " "+cls;
}

function removeClass(ele,cls) {
    if (hasClass(ele,cls)) {
        var reg = new RegExp('(\\s|^)'+cls+'(\\s|$)');
        ele.className=ele.className.replace(reg,' ');
    }
}

function hasClass(ele,cls) {
    return ele.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'));
}

/**
 * ReplaceAll by Fagner Brack (MIT Licensed)
 * Replaces all occurrences of a substring in a string
 */
String.prototype.replaceAll = function( token, newToken, ignoreCase ) {
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