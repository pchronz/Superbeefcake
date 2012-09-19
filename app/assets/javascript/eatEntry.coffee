@initEatEntry = () ->
  ids = {}
  $("#link-clear-date").click(() ->
      $("#modal-clear-sheet-warning").modal("show");
  );

  # get the id of the chosen food and set it for the submission
  $("#new-macro-entry").submit(() ->
    name = $("input[name='foodName']").attr("value")
    id = ids[name]
    $("input[name='foodId']").attr('value', id)
  )

  # show the modal for entering a macro entry manually
  $("#link-manual-macro").click(() -> $("#modal-macro-manual").modal("show"))

  # select the navbar link for this view as active
  $("#eat-nav-link").closest("li").addClass("active")

  # typeahead source
  updateQuery = (typeahead, query) -> 
      $.getJSON(suggestFoodUrl + query, (response) ->
          ids = {}
          names = []
          for item in response.items
            ids[item.name] = item.id
            names.push(item.name)
          typeahead.process(names)
      )

  # typeahead
  $("input[name='foodName']").typeahead({
    source: updateQuery,
    matcher: (item) -> return true
  })

  # focus the food input field
  $("input[name='foodName']").focus()

  regMacroEntry = (macroId) ->
    tooltipQuery = "#amount-" + macroId;
    $(tooltipQuery).tooltip({trigger: "focus"});
    $(tooltipQuery).blur(() ->
        # status label
        $("#label-saving-" + macroId).addClass('hide');
        $("#label-saved-" + macroId).addClass('hide');
    );
    $(tooltipQuery).change(() ->
        # status label
        $("#label-saved-" + macroId).addClass('hide');
        $("#label-saving-" + macroId).removeClass('hide');
        $(this).tooltip('hide');

        newAmount = $(this).val();
        $.get("@routes.Application.updateMacroEntry(macroEntry.id.get, 0)" + newAmount, (data) ->
          # status label
          $("#label-saving-" + macroId).addClass('hide');
          $("#label-saved-" + macroId).removeClass('hide');

          resp = eval(data)[0];
          # update the entry in the view
          $("#td-energy-" + macroId).html(resp.energy);
          $("#td-protein-" + macroId).html(resp.protein);
          $("#td-fat-" + macroId).html(resp.fat);
          $("#td-carbs-" + macroId).html(resp.carbs);

          # update the total in the view as well
          # amount
          totalAmount = 0;
          $("input[id^='amount-']").each(() ->
              totalAmount += Number($(this).val());
          );
          $("#amount-total").html(totalAmount);

          # energy, protein, fat, carbs
          fields = ["energy", "protein", "fat", "carbs"];
          for field in fields
              total= 0;
              $("td[id^='td-" + field + "-']").each(() ->
                  total+= Number($(this).html());
              );
              $("#" + field + "-total").html(total);
          
        );
    );


