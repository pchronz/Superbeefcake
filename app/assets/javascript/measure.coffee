@initMeasure = () ->
  $("select[name^='start'], select[name^='end']").change(() -> 
      $(this).closest("form").submit()
  )
  $("input[name='day']").tooltip({trigger: 'focus'})
  $("input[name='month']").tooltip({trigger: 'focus'})
  $("input[name='year']").tooltip({trigger: 'focus'})

  $("#form-update").submit(() -> 
    return false
  )

@regMeasureEntry = (measureEntryId, updateMeasureEntryUrl) ->
  tooltipQuery = "#measure-entry-" + measureEntryId

  # tooltip
  $(tooltipQuery).tooltip({trigger: "focus", title: "Press Enter to save"})

  $(tooltipQuery).focus(() -> 
      # status label
      $("#label-saving-" + measureEntryId).addClass('hide')
      $("#label-saved-" + measureEntryId).addClass('hide')
  )
  $(tooltipQuery).blur(() ->
      # status label
      $("#label-saving-" + measureEntryId).addClass('hide')
      $("#label-saved-" + measureEntryId).addClass('hide')
  )

  # ajax call
  $(tooltipQuery).change(() -> 
      # status label
      $("#label-saved-" + measureEntryId).addClass('hide')
      $("#label-saving-" + measureEntryId).removeClass('hide')
      $(this).tooltip('hide')
      # read the value and send the AJAX request
      newVal = $(this).val()
      $.get(updateMeasureEntryUrl + newVal, (date) -> 
          # status label
          $("#label-saving-" + measureEntryId).addClass('hide')
          $("#label-saved-" + measureEntryId).removeClass('hide')
      )
  )

  # select the navbar link for this view as active
  $("#measure-nav-link").closest("li").addClass("active")

  # put the focus on the first input field
  $("#input-weight-new").focus()

