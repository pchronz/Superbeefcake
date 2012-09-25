@initEat = () ->
  $("select[name='start-day'], select[name='start-month'], select[name='start-year']").change(() -> 
    console.log('submitting via date select')
    $(this).closest("form").submit()
  )
  $("#date-prev-start, #date-today-start, #date-next-start").click(()->
    console.log('submitting via date picker')
    $(this).closest("form").submit()
  )

  
