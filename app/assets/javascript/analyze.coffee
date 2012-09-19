@initAnalyze = () ->
  # select the navbar link for this view as active
  $("#analyze-nav-link").closest("li").addClass("active")

  $("select[name='startday'], select[name='startmonth'], select[name='startyear']").change(() ->
    $(this).closest("form").submit()
  )

  $("select[name='endday'], select[name='endmonth'], select[name='endyear']").change(() ->
    $(this).closest("form").submit()
  )

  @activeQuery = "#input-energy-goal"
  knownQueries = []
  selectGoal = (query) ->
      $(activeQuery).siblings("input[type='radio']").attr('checked', 'checked')
  initQueries = () -> 
    inputs = $("input[id$='goal']")
    for input in inputs 
      query = "#" + $(input).attr('id')
      knownQueries.push(query)
    activeQuery = if knownQueries.length > 0 then knownQueries[0]
  

  initTooltips = () ->
    for query in knownQueries
      $(query).tooltip({trigger: 'manual'})
      $(query).bind("keypress mouseup", (event) -> 
        if not event.which == 13 
          el = $(this)
          setTimeout(() -> $(el).tooltip('show'),
          500)
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
        activeQuery = "#" + $(this).attr('id')
        selectGoal(activeQuery)
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

      selectGoal(activeQuery)

