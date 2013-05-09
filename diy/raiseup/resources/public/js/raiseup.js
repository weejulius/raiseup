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

(function ($) {
  var websocket = $.websocket("ws://127.0.0.1:8080/todo/slots",{
    events:{
      message:function(e){
        if(e.data.errors){
          showAlert($('#slot-new-msg'),e.data.errors);
        }else{
          var appendedSlot = '<li>'+val($('#description'))+' '+val($('#start-time'))+' '+val($('#estimation'))+'</li>';
          $('#module-planned-slot-list').append(appendedSlot);
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
    $('#add-task-slot').click(function(e){
      $('#slot-new-msg').removeClass('error-msg in alert').text('')
      websocket.send('message',
                     readInputsToJson("description","estimation","start-time"));
    });
})(jQuery);
