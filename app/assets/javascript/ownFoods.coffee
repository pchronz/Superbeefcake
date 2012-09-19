@initOwnFoods = (updateFoodUrl) ->
  activeInputId = ""
  # perform AJAX call when save button is clicked
  updateEntry = (id) -> 
    url = updateFoodUrl
    data = {}
    data['id'] = $("#id-" + id).attr('value')
    data['name'] = $("#name-" + id).attr('value')
    data['amount'] = $("#amount-" + id).attr('value')
    data['energy'] = $("#energy-" + id).attr('value')
    data['protein'] = $("#protein-" + id).attr('value')
    data['fat'] = $("#fat-" + id).attr('value')
    data['carbs'] = $("#carbs-" + id).attr('value')
    $.get(url, data, (data, textStatus, jqXHR) -> 
        # on success remove the tooltip
        $("#" + activeInputId).tooltip("hide")
        $("#unsave-" + id).hide()
        $("#save-" + id).show()
    )

  # initialize all tooltips
  $(".food-input").tooltip({trigger: 'manual', placement: 'bottom'})
  # show save button on value change
  $(".food-input").bind("keypress change focus", (e) -> 
    id = $(this).attr('id')
    foodName = id.split('-')[1]
    $("#save-" + foodName).hide()
    $("#unsave-" + foodName).show()
    if e.keyCode == 13 
      if foodName == "new" 
        submitNewEntry()
      else 
        updateEntry(foodName)
    if activeInputId != id 
      $(this).tooltip("show")
      $("#" + activeInputId).tooltip("hide")
      activeInputId = id
  )
  submitNewEntry = () -> 
    fail = false

    # set the values of the hidden form to equal the entered ones
    fields = ["name", "amount", "energy", "protein", "fat", "carbs"]
    for field in fields 
      value = $("#" + field + "-new").attr("value")
      $("#" + field + "-hidden").attr("value", value)
      fail = if value == "" or value == undefined then true

    if not fail 
      # submit
      $("#hidden-form").submit()
    else 
      # TODO nice error box
      alert("Better check yoself, your values do not look to good!")
  return

