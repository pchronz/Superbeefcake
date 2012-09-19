@initEat = () ->
  $("select[name='day'], select[name='month'], select[name='year']").change(() -> 
    $(this).closest("form").submit()
  )

  
