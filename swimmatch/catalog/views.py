from django.shortcuts import render

# Create your views here.
from .models import Swimsuit, SwimCap

def index(request):
    products = Swimsuit.objects.all()[:4]

    context = {
        'products': products,
    }

    # Render the HTML template index.html with the data in the context variable
    return render(request, 'index.html', context=context)