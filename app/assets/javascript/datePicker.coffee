@initDatePicker = (id) ->
  readDate = () ->
    setYear = $("#year-" + id).val()
    setMonth = $("#month-" + id).val()
    setDay = $("#day-" + id).val()
    setDate = new Date(setYear, setMonth-1, setDay)
  writeDate = (date) ->
    $("#year-"+id).val(date.getFullYear())
    $("#month-"+id).val(date.getMonth()+1)
    $("#day-"+id).val(date.getDate())
    
  $("#date-prev-" + id).click(() -> 
      # read current date
      date = readDate()
      # decrement by a day
      date.setDate(date.getDate()-1)
      # write back
      writeDate(date)
      false
  )
  $("#date-today-" + id).click(() ->
      writeDate(new Date())
      false
  )
  $("#date-next-" + id).click(() -> 
      # read current date
      date = readDate()
      # increment by a day
      date.setDate(date.getDate()+1)
      # write back
      writeDate(date)
      false
  )

