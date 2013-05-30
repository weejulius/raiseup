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
function hilightCurrent(el){
    el
    .on({
      mouseenter: function(){$(this).addClass('current');},
      mouseleave: function(){$(this).removeClass('current');}
    });
}
(function ($) {

  //events on slot list
  hilightCurrent($('#module-unplanned-slot-list li'));


  var websocket = $.websocket("ws://127.0.0.1:8080/todo/slots",{
    events:{
      message:function(e){
        if(e.data.errors){
          showAlert($('#slot-new-msg'),e.data.errors);
        }else{
          var appendedSlot = '<li><span>'+val($('#description'))+'</span><button class="hidden"></button></li>';
          $(appendedSlot).prependTo($('#module-unplanned-slot-list'));
          hilightCurrent($('#module-unplanned-slot-list li'));
          resetForm('module-new-slot');
          $('#slot-new-msg').html('<i class="icon-ok"></i>');
          $('#slot-new-msg').addClass('in');
          window.setTimeout(function(){
            $('#slot-new-msg').removeClass('in');
          },3000);
        }
      }
    }
  });

  //events to add slot
  $('#add-task-slot').click(function(e){
    $('#slot-new-msg').removeClass('error-msg in alert').text('');
    websocket.send('message',
                     readInputsToJson("description"));
    });

})(jQuery);
