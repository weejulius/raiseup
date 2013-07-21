function readInputsToJson(){
  var json = {};
  for (var i = 0; i < arguments.length; i++) {
    inputName = arguments[i];
    json[inputName]=$('input[name='+inputName+']').val();
  }
  return json;
}

function resetForm(id){
 $('#'+id+' input')
 .not(':button, :submit, :reset, :hidden')
 .val('')
 .removeAttr('checked')
 .removeAttr('selected');
}

function showAlert(el,msg){
  el.text(msg);
  el.addClass("alert in error-msg");
}

function val(input){
  var value = input.val();
  if(value){
    return value;
  }else{
    return '';
  }
}

//add event clicking the button to delete slot
function onDeleteTaskSlotEvent(websocket){
    $('#module-unplanned-slot-list').on("click", ".hidden", function(e){
    var idval= $(this).parent().attr('id').substring(5);
    var json={};
    json['ar-id'] = idval;
    websocket.send('delete-task-slot',json);
  });
 }


//add event clicking the button to start slot
function onClickToStartTaskSlot(websocket){
    $('#module-unplanned-slot-list').on("click", ".start", function(e){
    var idval= $(this).parent().attr('id').substring(5);
    var json={};
    json['ar-id'] = idval;
    websocket.send('start-task-slot',json);
  });
 }

//hilight the current slot focused on
function hilightCurrent(el){
  el.on( "mouseenter","li",
         function(e){
           $(this).addClass('current');
         }
       );
  el.on( "mouseleave","li",
         function(e){
           $(this).removeClass('current');
         }
       );
}



function onSlotCreated(e){
  if(e.data.errors){
    showAlert($('#slot-new-msg'),e.data.errors);
  }else{
    var appendedSlot = '<li id="slot-'+e.data+'"><button class="start">></button><span>'+val($('#description'))+'</span><button class="hidden"></button></li>';
    $(appendedSlot).prependTo($('#module-unplanned-slot-list'));
    resetForm('module-new-slot');
    $('#slot-new-msg').html('<i class="icon-ok"></i>');
    $('#slot-new-msg').addClass('in');
    window.setTimeout(function(){
      $('#slot-new-msg').removeClass('in');
    },3000);
  }
}

function getWebsocketUrl(scheme,hostName,port){
  scheme=scheme=='https'?"wss":"ws";
  if(!port){
    port = '8000';//port for openshift
    if(scheme=='wss'){
      port = '8443';//security webscoket port for openshift
    }
  }
  return scheme+"://"+hostName+":"+port+"/ws";
}

function onSlotDeleted(e){
  $('#module-unplanned-slot-list li.current').remove();
}



function onSlotStarted(e){
  $('#module-unplanned-slot-list li.current').prependTo($('#module-planned-slot-list'));
}

(function ($) {

  //events on slot list
  hilightCurrent($('#module-unplanned-slot-list'));

  var websocket = $.websocket(getWebsocketUrl(location.scheme,location.hostname,location.port),{
    events:{
      create_task_slot:onSlotCreated,
      delete_task_slot:onSlotDeleted,
      start_task_slot:onSlotStarted
    }
  });

  //events to add slot
  $('#add-task-slot').click(function(e){
    $('#slot-new-msg').removeClass('error-msg in alert').text('');
    websocket.send('create-task-slot',
                   readInputsToJson("description"));
    });

  onDeleteTaskSlotEvent(websocket);
  onClickToStartTaskSlot(websocket);
})(jQuery);
