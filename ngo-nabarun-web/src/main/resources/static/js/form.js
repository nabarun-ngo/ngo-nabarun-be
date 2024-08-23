(function($) {
	"use strict";




	//join us form validation
	$('#JoinUsForm').validate({ // initialize the plugin
		rules: {
			firstName: {
				required: true,
				rangelength: [2, 20]
			},
			lastName: {
				required: true,
				rangelength: [2, 20]
			},
			email: {
				required: true,
				email: true
			},
			contactNumber: {
				required: true,
				number: true,
				minlength: mobileNoMinLengthValidator,
				maxlength: mobileNoMaxLengthValidator
			},
			hometown: {
				required: true,
				minlength: 5,
				maxlength: 30
			},
			howDoUKnowAboutNabarun: {
				required: true,
				minlength: 5,
				maxlength: 100
			},
			reasonForJoining: {
				required: true,
				minlength: 5,
				maxlength: 100
			},
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});

	$('#phonecode').change(function(c) {
		$('#mobileno').val('');
	})

	function mobileNoMinLengthValidator() {
		let code = $('#phonecode').val();
		if (code === '91') {
			return 10;
		} else {
			return 8;
		}
	}

	function mobileNoMaxLengthValidator() {
		let code = $('#phonecode').val();
		if (code === '91') {
			return 10;
		} else {
			return 13;
		}
	}





	function getFormData($form) {
		var unindexed_array = $form.serializeArray();
		var indexed_array = {};
		$.map(unindexed_array, function(n, i) {
			indexed_array[n['name']] = n['value'];
		});
		return indexed_array;
	}




	$('#passwordForm').validate({ // initialize the plugin
		rules: {

			email: {
				required: true,
				email: true
			},
			password: {
				required: true,
				minlength: 6,
				maxlength: 30
			},
			confirmPassword: {
				required: true,
				equalTo: "#password"
			}
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});


	$('#ContactUsForm').validate({ // initialize the plugin
		rules: {
			firstName: {
				required: true,
				rangelength: [2, 20]
			},
			email: {
				required: true,
				email: true
			},
			contactNumber: {
				required: true,
				number: true,
			},
			subject: {
				required: true,
				rangelength: [2, 50]
			},
			message: {
				required: true,
				rangelength: [5, 500]
			},
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});


	$('#donationForm').validate({ // initialize the plugin
		rules: {
			firstName: {
				required: true,
				//rangelength: [2, 20]
			},
			email: {
				required: true,
				email: true
			},
			contactNumber: {
				required: true,
				number: true,
			},
			amount: {
				required: true,
				number: true,
			},
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});


	$('.check-item-1').click(function() {
		console.log("hi 1" + this.value)
		var checked_status = this.checked;
		if (checked_status == true) {
			$("#sub_bt1").removeAttr("disabled");
			//paidToAccountId
		} else {
			$("#sub_bt1").attr("disabled", "disabled");
		}
	});

	$('.check-item-2').click(function() {
		console.log("hi 2")
		var checked_status = this.checked;
		if (checked_status == true) {
			$("#sub_bt2").removeAttr("disabled");
		} else {
			$("#sub_bt2").attr("disabled", "disabled");
		}
	});

	/*
	$('#donate_upi').add('#donate_bank').validate({
		rules: {
			paidToAcc: {
				required: true,
			}
		},
		messages:{
			paidToAcc: {
				required: 'Please select the confirmation checkbox',
			}
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});*/

	$("input:checkbox").on('click', function() {
		// in the handler, 'this' refers to the box clicked on
		var $box = $(this);
		if ($box.is(":checked")) {
			// the name of the box is retrieved using the .attr() method
			// as it is assumed and expected to be immutable
			var group = "input:checkbox[name='" + $box.attr("name") + "']";
			// the checked state of the group/box on the other hand will change
			// and the current value is retrieved using .prop() method
			$(group).prop("checked", false);
			$box.prop("checked", true);
		} else {
			$box.prop("checked", false);
		}
	});



})(jQuery); 


