$(function(){
  let $body = $(document.body);
  _.delay(function(){
    $body.addClass("load-done")
  }, 0)

  let index = _.random(0,10);
  let bgUrl = `url('img/bg${index}.jpg')`
  $body.css({
    "background-image" : bgUrl
  })
})