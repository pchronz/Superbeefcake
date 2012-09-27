@initAnalyze = () ->
  # select the navbar link for this view as active
  $("#analyze-nav-link").closest("li").addClass("active")

  $("select[name='start-day'], select[name='start-month'], select[name='start-year']").change(() ->
    $(this).closest("form").submit()
  )
  $("#date-prev-start, #date-today-start, #date-next-start").click(() ->
    $(this).closest("form").submit()
  )

  $("select[name='end-day'], select[name='end-month'], select[name='end-year']").change(() ->
    $(this).closest("form").submit()
  )
  $("#date-prev-end, #date-today-end, #date-next-end").click(() ->
    $(this).closest("form").submit()
  )

  window.activeQuery = "#input-energy-goal"
  knownQueries = []
  selectGoal = (query) ->
    $(query).siblings("input[type='radio']").attr('checked', 'checked')

  initQueries = () -> 
    inputs = $("input[id$='goal']")
    for input in inputs 
      query = "#" + $(input).attr('id')
      knownQueries.push(query)
    window.activeQuery = if knownQueries.length > 0 then knownQueries[0]
  

  initTooltips = () ->
    for query in knownQueries
      $("*[rel='tooltip']").tooltip({trigger: 'manual'})
      $(query).bind("keypress mouseup", (event) -> 
        if event.which != 13 
          el = $(this)
          setTimeout(() -> 
            $(el).tooltip('show')
          , 500)
      )

      $(query).bind("keypress", (event) -> 
        goalName = $(this).attr('id').split("-")[1]
        if event.which == 13 
          newGoal = $(this).val()
          el = $(this)
          $.get("/updateGoal/" + goalName + "/" + newGoal, () ->
            el.tooltip('hide')
            plotEnergyWeight()
          )
      )

  $("input[id$='goal']").focus(() -> 
    window.activeQuery = "#" + $(this).attr('id')
    selectGoal(window.activeQuery)
    plotEnergyWeight()
  )

  # make sure the page will not be reloaded when the user hits enter
  $("#form-update").submit(() -> 
    false
  )

  # initially activate the selected radio button
  initQueries()

  # init all tooltips
  initTooltips()

  selectGoal(window.activeQuery)

